package org.agri.agritrade.service.impl;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import lombok.extern.slf4j.Slf4j;
import org.agri.agritrade.dto.ResponseStructure;
import org.agri.agritrade.dto.request.PaymentVerificationRequest;
import org.agri.agritrade.dto.response.RazorpayOrderResponse;
import org.agri.agritrade.entity.Bid;
import org.agri.agritrade.entity.CropBatch;
import org.agri.agritrade.entity.User;
import org.agri.agritrade.entity.enums.BidStatus;
import org.agri.agritrade.entity.enums.CropStatus;
import org.agri.agritrade.entity.enums.OrderStatus;
import org.agri.agritrade.entity.enums.PaymentStatus;
import org.agri.agritrade.repository.BidRepository;
import org.agri.agritrade.repository.CropBatchRepository;
import org.agri.agritrade.repository.OrderRepository;
import org.agri.agritrade.repository.PaymentRepository;
import org.agri.agritrade.service.RazorpayServicePort;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class RazorpayService implements RazorpayServicePort {

    private final String keyId;
    private final String keySecret;
    private final BidRepository bidRepository;
    private final CropBatchRepository cropBatchRepository;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;

    public RazorpayService(
            @Value("${razorpay.key.id}") String keyId,
            @Value("${razorpay.key.secret}") String keySecret,
            BidRepository bidRepository,
            CropBatchRepository cropBatchRepository,
            OrderRepository orderRepository,
            PaymentRepository paymentRepository) {
        this.keyId = keyId;
        this.keySecret = keySecret;
        this.bidRepository = bidRepository;
        this.cropBatchRepository = cropBatchRepository;
        this.orderRepository = orderRepository;
        this.paymentRepository = paymentRepository;
    }
    @Override
    public ResponseStructure<RazorpayOrderResponse> createOrder(Long bidId) {
        Optional<Bid> bidOpt = bidRepository.findById(bidId);
        if (bidOpt.isEmpty()) {
            return new ResponseStructure<>(HttpStatus.NOT_FOUND.value(), "Bid not found", null);
        }

        Bid bid = bidOpt.get();
        if (bid.getBidStatus() != BidStatus.ACCEPTED) {
            return new ResponseStructure<>(HttpStatus.BAD_REQUEST.value(),
                    "Bid must be accepted by farmer before payment", null);
        }

        // Check bid quantity doesn't exceed available crop quantity
        if (bid.getBidQuantity().compareTo(bid.getCropBatch().getQuantity()) > 0) {
            return new ResponseStructure<>(HttpStatus.BAD_REQUEST.value(),
                    "Bid quantity exceeds available crop quantity (" + bid.getCropBatch().getQuantity() + ")", null);
        }

        BigDecimal totalAmount = bid.getBidAmount().multiply(bid.getBidQuantity());
        int amountInPaise = totalAmount.multiply(BigDecimal.valueOf(100)).intValue();

        try {
            RazorpayClient client = new RazorpayClient(keyId, keySecret);
            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", amountInPaise);
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", "bid_" + bidId);

            Order razorpayOrder = client.orders.create(orderRequest);

            User retailer = bid.getRetailer();

            RazorpayOrderResponse response = new RazorpayOrderResponse();
            response.setRazorpayOrderId(razorpayOrder.get("id"));
            response.setAmount(totalAmount);
            response.setCurrency("INR");
            response.setKey(keyId);
            response.setBidId(bidId);
            response.setRetailerName(retailer.getFullName());
            response.setRetailerPhone(retailer.getPhoneNumber());
            response.setRetailerEmail(retailer.getEmail());
            response.setCropName(bid.getCropBatch().getCropName());

            return new ResponseStructure<>(HttpStatus.OK.value(), "Razorpay order created", response);
        } catch (RazorpayException e) {
            log.error("Razorpay order creation failed: {}", e.getMessage(), e);
            return new ResponseStructure<>(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Payment order creation failed", null);
        }
    }
    @Override
    @Transactional
    public ResponseStructure<String> verifyAndProcessPayment(PaymentVerificationRequest request) {
        // 1. Verify Razorpay signature
        String payload = request.getRazorpayOrderId() + "|" + request.getRazorpayPaymentId();
        if (!verifySignature(payload, request.getRazorpaySignature())) {
            return new ResponseStructure<>(HttpStatus.BAD_REQUEST.value(), "Payment verification failed", null);
        }

        // 2. Find the bid
        Optional<Bid> bidOpt = bidRepository.findById(request.getBidId());
        if (bidOpt.isEmpty()) {
            return new ResponseStructure<>(HttpStatus.NOT_FOUND.value(), "Bid not found", null);
        }

        Bid acceptedBid = bidOpt.get();
        CropBatch crop = acceptedBid.getCropBatch();

        // 3. Deduct bid quantity from crop's available quantity
        BigDecimal remainingQuantity = crop.getQuantity().subtract(acceptedBid.getBidQuantity());

        if (remainingQuantity.compareTo(BigDecimal.ZERO) <= 0) {
            // All quantity sold — mark SOLD and reject all pending bids
            crop.setQuantity(BigDecimal.ZERO);
            crop.setStatus(CropStatus.SOLD);

            List<Bid> pendingBids = bidRepository.findByCropBatch_IdAndBidStatus(crop.getId(), BidStatus.PENDING);
            for (Bid otherBid : pendingBids) {
                otherBid.setBidStatus(BidStatus.REJECTED);
                bidRepository.save(otherBid);
            }
        } else {
            // Partial quantity remains — keep crop available
            crop.setQuantity(remainingQuantity);
            crop.setStatus(CropStatus.AVAILABLE);

            // Reject pending bids whose quantity exceeds the remaining quantity
            List<Bid> pendingBids = bidRepository.findByCropBatch_IdAndBidStatus(crop.getId(), BidStatus.PENDING);
            for (Bid otherBid : pendingBids) {
                if (otherBid.getBidQuantity().compareTo(remainingQuantity) > 0) {
                    otherBid.setBidStatus(BidStatus.REJECTED);
                    bidRepository.save(otherBid);
                }
            }
        }
        cropBatchRepository.save(crop);

        // 5. Create order from the accepted bid
        org.agri.agritrade.entity.Order order = new org.agri.agritrade.entity.Order();
        order.setBid(acceptedBid);
        order.setCropBatch(crop);
        order.setFarmer(crop.getFarmer());
        order.setRetailer(acceptedBid.getRetailer());
        order.setFinalAmount(acceptedBid.getBidAmount().multiply(acceptedBid.getBidQuantity()));
        order.setQuantity(acceptedBid.getBidQuantity());
        order.setOrderStatus(OrderStatus.CONFIRMED);
        order.setPaymentStatus(PaymentStatus.COMPLETED);
        order.setDeliveryAddress(acceptedBid.getRetailer().getAddress());
        order.setOrderDate(LocalDateTime.now());
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        org.agri.agritrade.entity.Order savedOrder = orderRepository.save(order);

        // 6. Create payment record
        org.agri.agritrade.entity.Payment payment = new org.agri.agritrade.entity.Payment();
        payment.setOrder(savedOrder);
        payment.setAmount(savedOrder.getFinalAmount());
        payment.setPaymentMethod("RAZORPAY_UPI");
        payment.setPaymentStatus(PaymentStatus.COMPLETED);
        payment.setTransactionId(request.getRazorpayPaymentId());
        payment.setPaymentDate(LocalDateTime.now());
        payment.setCreatedAt(LocalDateTime.now());
        paymentRepository.save(payment);

        return new ResponseStructure<>(HttpStatus.OK.value(), "Payment verified and order created", null);
    }

    private boolean verifySignature(String payload, String expectedSignature) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(keySecret.getBytes(), "HmacSHA256");
            mac.init(secretKey);
            byte[] hash = mac.doFinal(payload.getBytes());
            String generatedSignature = HexFormat.of().formatHex(hash);
            return generatedSignature.equals(expectedSignature);
        } catch (Exception e) {
            log.error("Signature verification error: {}", e.getMessage());
            return false;
        }
    }
}
