package org.agri.agritrade.service;
import org.agri.agritrade.dto.PagedResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.agri.agritrade.dto.BidDTO;
import org.agri.agritrade.dto.ResponseStructure;
import org.agri.agritrade.entity.Bid;
import org.agri.agritrade.entity.CropBatch;
import org.agri.agritrade.entity.User;
import org.agri.agritrade.entity.enums.BidStatus;
import org.agri.agritrade.entity.enums.CropStatus;
import org.agri.agritrade.repository.BidRepository;
import org.agri.agritrade.repository.CropBatchRepository;
import org.agri.agritrade.repository.OrderRepository;
import org.agri.agritrade.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BidService {

    private final BidRepository bidRepository;
    private final CropBatchRepository cropBatchRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final SmsService smsService;

    private BidDTO toDTO(Bid bid) {
        BidDTO dto = new BidDTO();
        dto.setId(bid.getId());
        dto.setCropBatchId(bid.getCropBatch().getId());
        dto.setCropBatchName(bid.getCropBatch().getCropName());
        dto.setRetailerId(bid.getRetailer().getId());
        dto.setRetailerName(bid.getRetailer().getFullName());
        dto.setBidAmount(bid.getBidAmount());
        dto.setBidQuantity(bid.getBidQuantity());
        dto.setBidStatus(bid.getBidStatus());
        dto.setBidDate(bid.getBidDate());
        dto.setCreatedAt(bid.getCreatedAt());
        dto.setPaid(orderRepository.findByBid_Id(bid.getId()).isPresent());
        return dto;
    }

    @Transactional
    public ResponseStructure<BidDTO> createBid(BidDTO dto) {
        Optional<CropBatch> cropOpt = cropBatchRepository.findById(dto.getCropBatchId());
        if (dto.getBidAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return new ResponseStructure<>(HttpStatus.BAD_REQUEST.value(), "Invalid bid amount", null);
        }

        if (dto.getBidQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            return new ResponseStructure<>(HttpStatus.BAD_REQUEST.value(), "Invalid bid quantity", null);
        }
        if (cropOpt.isEmpty())
            return new ResponseStructure<>(HttpStatus.NOT_FOUND.value(), "Crop batch not found", null);

        CropBatch crop = cropOpt.get();
        if (crop.getStatus() != CropStatus.AVAILABLE)
            return new ResponseStructure<>(HttpStatus.BAD_REQUEST.value(), "Crop is not available for bidding", null);

        if (dto.getBidQuantity().compareTo(crop.getQuantity()) > 0)
            return new ResponseStructure<>(HttpStatus.BAD_REQUEST.value(),
                    "Bid quantity exceeds available quantity (" + crop.getQuantity() + ")", null);

        Optional<User> retailerOpt = userRepository.findById(dto.getRetailerId());
        if (retailerOpt.isEmpty())
            return new ResponseStructure<>(HttpStatus.NOT_FOUND.value(), "Retailer not found", null);

        Bid bid = new Bid();
        bid.setCropBatch(crop);
        bid.setRetailer(retailerOpt.get());
        bid.setBidAmount(dto.getBidAmount());
        bid.setBidQuantity(dto.getBidQuantity());
        bid.setBidStatus(BidStatus.PENDING);
        bid.setBidDate(LocalDateTime.now());
        bid.setCreatedAt(LocalDateTime.now());

        Bid saved = bidRepository.save(bid);

        // Send SMS to farmer notifying about new bid
        User farmer = crop.getFarmer();
        String farmerMsg = String.format(
                "New bid received on your crop '%s'! Retailer: %s, Amount: ₹%s, Quantity: %s %s. Check your dashboard to review.",
                crop.getCropName(), retailerOpt.get().getFullName(),
                dto.getBidAmount(), dto.getBidQuantity(), crop.getUnit());
        smsService.sendSms(farmer.getPhoneNumber(), farmerMsg);

        return new ResponseStructure<>(HttpStatus.CREATED.value(), "Bid placed successfully", toDTO(saved));
    }

    public ResponseStructure<List<BidDTO>> getBidsByCropBatch(Long cropBatchId) {
        List<BidDTO> bids = bidRepository.findByCropBatch_Id(cropBatchId).stream().map(this::toDTO).toList();
        return new ResponseStructure<>(HttpStatus.OK.value(), "Bids retrieved", bids);
    }

    public ResponseStructure<List<BidDTO>> getBidsByRetailer(Long retailerId) {
        List<BidDTO> bids = bidRepository.findByRetailer_Id(retailerId).stream().map(this::toDTO).toList();
        return new ResponseStructure<>(HttpStatus.OK.value(), "Bids retrieved", bids);
    }

    @Transactional
    public ResponseStructure<BidDTO> updateBidStatus(Long bidId, BidStatus status) {
        Optional<Bid> bidOpt = bidRepository.findById(bidId);
        if (bidOpt.isEmpty())
            return new ResponseStructure<>(HttpStatus.NOT_FOUND.value(), "Bid not found", null);

        Bid bid = bidOpt.get();
        bid.setBidStatus(status);
        // No longer close bidding on accept — crop stays available for partial selling
        Bid updatedBid = bidRepository.save(bid);

        // Send SMS to retailer notifying about bid acceptance/rejection
        User retailer = bid.getRetailer();
        String cropName = bid.getCropBatch().getCropName();
        String statusText = status == BidStatus.ACCEPTED ? "accepted" : "rejected";
        String retailerMsg = String.format(
                "Your bid on '%s' has been %s by the farmer. Bid Amount: ₹%s, Quantity: %s. %s",
                cropName, statusText, bid.getBidAmount(), bid.getBidQuantity(),
                status == BidStatus.ACCEPTED ? "Proceed to payment on your dashboard." : "You can place new bids on other crops.");
        smsService.sendSms(retailer.getPhoneNumber(), retailerMsg);

        return new ResponseStructure<>(HttpStatus.OK.value(), "Bid status updated", toDTO(updatedBid));
    }

    @Transactional
    public ResponseStructure<Void> deleteBid(Long bidId) {
        if (!bidRepository.existsById(bidId))
            return new ResponseStructure<>(HttpStatus.NOT_FOUND.value(), "Bid not found", null);
        bidRepository.deleteById(bidId);
        return new ResponseStructure<>(HttpStatus.OK.value(), "Bid deleted", null);
    }

    public ResponseStructure<PagedResponse<BidDTO>> getBidsByCropBatchPaged(Long cropBatchId, int page, int size) {
        Page<Bid> bidPage = bidRepository.findByCropBatch_Id(cropBatchId,
                org.springframework.data.domain.PageRequest.of(page, size,
                        org.springframework.data.domain.Sort.by("createdAt").descending()));
        List<BidDTO> dtos = bidPage.getContent().stream().map(this::toDTO).toList();
        PagedResponse<BidDTO> paged = new PagedResponse<>(
                dtos, bidPage.getNumber(), bidPage.getSize(),
                bidPage.getTotalElements(), bidPage.getTotalPages(), bidPage.isLast());
        return new ResponseStructure<>(HttpStatus.OK.value(), "Bids retrieved", paged);
    }

    public ResponseStructure<PagedResponse<BidDTO>> getBidsByRetailerPaged(Long retailerId, int page, int size) {
        Page<Bid> bidPage = bidRepository.findByRetailer_Id(retailerId,
                org.springframework.data.domain.PageRequest.of(page, size,
                        org.springframework.data.domain.Sort.by("createdAt").descending()));
        List<BidDTO> dtos = bidPage.getContent().stream().map(this::toDTO).toList();
        PagedResponse<BidDTO> paged = new PagedResponse<>(
                dtos, bidPage.getNumber(), bidPage.getSize(),
                bidPage.getTotalElements(), bidPage.getTotalPages(), bidPage.isLast());
        return new ResponseStructure<>(HttpStatus.OK.value(), "Bids retrieved", paged);
    }
}