package org.agri.agritrade.controller;

import lombok.RequiredArgsConstructor;
import org.agri.agritrade.dto.BidDTO;
import org.agri.agritrade.dto.ResponseStructure;
import org.agri.agritrade.entity.enums.BidStatus;
import org.agri.agritrade.service.BidService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bids")
@RequiredArgsConstructor
public class BidController {

    private final BidService bidService;

    @PostMapping
    @PreAuthorize("hasRole('RETAILER')")
    public ResponseEntity<ResponseStructure<BidDTO>> createBid(@RequestBody BidDTO dto) {
        ResponseStructure<BidDTO> res = bidService.createBid(dto);
        return ResponseEntity.status(res.getStatusCode()).body(res);
    }

    @GetMapping("/crop/{cropBatchId}")
    public ResponseEntity<ResponseStructure<List<BidDTO>>> getByCrop(@PathVariable Long cropBatchId) {
        ResponseStructure<List<BidDTO>> res = bidService.getBidsByCropBatch(cropBatchId);
        return ResponseEntity.status(res.getStatusCode()).body(res);
    }

    @GetMapping("/retailer/{retailerId}")
    public ResponseEntity<ResponseStructure<List<BidDTO>>> getByRetailer(@PathVariable Long retailerId) {
        ResponseStructure<List<BidDTO>> res = bidService.getBidsByRetailer(retailerId);
        return ResponseEntity.status(res.getStatusCode()).body(res);
    }

    @PutMapping("/{id}/accept")
    @PreAuthorize("hasRole('FARMER')")
    public ResponseEntity<ResponseStructure<BidDTO>> acceptBid(@PathVariable Long id) {
        ResponseStructure<BidDTO> res = bidService.updateBidStatus(id, BidStatus.ACCEPTED);
        return ResponseEntity.status(res.getStatusCode()).body(res);
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasRole('FARMER')")
    public ResponseEntity<ResponseStructure<BidDTO>> rejectBid(@PathVariable Long id) {
        ResponseStructure<BidDTO> res = bidService.updateBidStatus(id, BidStatus.REJECTED);
        return ResponseEntity.status(res.getStatusCode()).body(res);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('RETAILER')")
    public ResponseEntity<ResponseStructure<Void>> deleteBid(@PathVariable Long id) {
        ResponseStructure<Void> res = bidService.deleteBid(id);
        return ResponseEntity.status(res.getStatusCode()).body(res);
    }
}