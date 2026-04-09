package org.agri.agritrade.entity;

import jakarta.persistence.*;
import lombok.*;
import org.agri.agritrade.util.enums.BidStatus;
import org.springframework.data.annotation.CreatedDate;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bids")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Bid {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "crop_batch_id", nullable = false)
    private CropBatch cropBatch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "retailer_id", nullable = false)
    private User retailer;

    @Column(name = "bid_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal bidAmount;

    @Column(name = "bid_quantity", nullable = false, precision = 10, scale = 2)
    private BigDecimal bidQuantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "bid_status", length = 20)
    private BidStatus bidStatus; // PENDING, ACCEPTED, REJECTED

    @Column(name = "bid_date")
    private LocalDateTime bidDate;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @OneToOne(mappedBy = "bid", cascade = CascadeType.ALL)
    private Order order;
}
