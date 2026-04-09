package org.agri.agritrade.repository;

import org.agri.agritrade.entity.Bid;
import org.agri.agritrade.util.enums.BidStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BidRepository extends JpaRepository<Bid, Long> {
    List<Bid> findByCropBatch_Id(Long cropBatchId);
    List<Bid> findByRetailer_Id(Long retailerId);
    List<Bid> findByCropBatch_IdAndBidStatus(Long cropBatchId, BidStatus status);
    Page<Bid> findByCropBatch_Id(Long cropBatchId, Pageable pageable);
    Page<Bid> findByRetailer_Id(Long retailerId, Pageable pageable);
}