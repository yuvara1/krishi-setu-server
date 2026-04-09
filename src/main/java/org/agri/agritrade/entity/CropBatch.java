package org.agri.agritrade.entity;

import jakarta.persistence.*;
import lombok.*;
import org.agri.agritrade.util.enums.CropStatus;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "crop_batches")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CropBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "farmer_id", nullable = false)
    private User farmer;

    @Column(name = "crop_name", nullable = false, length = 100)
    private String cropName;

    @Column(name = "crop_type", length = 50)
    private String cropType;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal quantity;

    @Column(name = "base_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal basePrice;

    @Column(name = "harvest_date")
    private LocalDate harvestDate;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private CropStatus status;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(length = 200)
    private String location;

    @Column(name = "is_organic")
    private Boolean isOrganic = false;

    @Column(length = 20)
    private String unit = "kg";

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "cropBatch", cascade = CascadeType.ALL)
    private List<Bid> bids;
}
