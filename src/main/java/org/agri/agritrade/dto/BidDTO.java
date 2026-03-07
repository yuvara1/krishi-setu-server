package org.agri.agritrade.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.agri.agritrade.entity.enums.BidStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class BidDTO {
    private Long id;

    @NotNull(message = "Crop batch ID is required")
    @Positive(message = "Crop batch ID must be positive")
    private Long cropBatchId;

    private String cropBatchName;


    @NotNull(message = "Retailer ID is required")
    @Positive(message = "Retailer ID must be positive")
    private Long retailerId;

    private String retailerName;

    @NotNull(message = "Bid amount is required")
    @DecimalMin(value = "0.01", message = "Bid amount must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Invalid bid amount format")
    private BigDecimal bidAmount;

    @NotNull(message = "Bid quantity is required")
    @DecimalMin(value = "0.01", message = "Bid quantity must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Invalid quantity format")
    private BigDecimal bidQuantity;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    private BidStatus bidStatus;
    private LocalDateTime bidDate;
    private LocalDateTime createdAt;
    private boolean paid;
}
