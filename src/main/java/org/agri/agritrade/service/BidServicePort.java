package org.agri.agritrade.service;

import org.agri.agritrade.dto.BidDTO;
import org.agri.agritrade.dto.PagedResponse;
import org.agri.agritrade.dto.ResponseStructure;
import org.agri.agritrade.entity.enums.BidStatus;

import java.util.List;

public interface BidServicePort {
    ResponseStructure<BidDTO> createBid(BidDTO dto);
    ResponseStructure<List<BidDTO>> getBidsByCropBatch(Long cropBatchId);
    ResponseStructure<List<BidDTO>> getBidsByRetailer(Long retailerId);
    ResponseStructure<BidDTO> updateBidStatus(Long bidId, BidStatus status);
    ResponseStructure<Void> deleteBid(Long bidId);
    ResponseStructure<PagedResponse<BidDTO>> getBidsByCropBatchPaged(Long cropBatchId, int page, int size);
    ResponseStructure<PagedResponse<BidDTO>> getBidsByRetailerPaged(Long retailerId, int page, int size);
}
