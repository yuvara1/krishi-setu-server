package org.agri.agritrade.repository;

import org.agri.agritrade.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByFarmer_Id(Long farmerId);
    List<Order> findByRetailer_Id(Long retailerId);
    Optional<Order> findByBid_Id(Long bidId);

    Page<Order> findByFarmer_Id(Long farmerId, Pageable pageable);
    Page<Order> findByRetailer_Id(Long retailerId, Pageable pageable);
}