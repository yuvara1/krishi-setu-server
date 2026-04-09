package org.agri.agritrade.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.agri.agritrade.util.enums.CropStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CropBatchDTO {
    private Long id;

    @NotNull(message = "Farmer ID is required")
    private Long farmerId;

    private String farmerName;
    private String farmerEmail;

    @NotBlank(message = "Crop name is required")
    @Size(max = 100, message = "Crop name must not exceed 100 characters")
    private String cropName;

    @Size(max = 50, message = "Crop type must not exceed 50 characters")
    private String cropType;

    @NotNull(message = "Quantity is required")
    @DecimalMin(value = "0.01", message = "Quantity must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Quantity format is invalid")
    private BigDecimal quantity;

    @NotNull(message = "Base price is required")
    @DecimalMin(value = "0.01", message = "Base price must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Base price format is invalid")
    private BigDecimal basePrice;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate harvestDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expiryDate;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    private CropStatus status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    private Integer totalBids = 0;
    private BigDecimal highestBidAmount = BigDecimal.ZERO;

    // Additional fields for enhanced functionality
    private String imageUrl;
    private String location;
    private Boolean isOrganic = false;
    private String unit = "kg"; // default unit

    // Utility methods
    public boolean isExpired() {
        return expiryDate != null && expiryDate.isBefore(LocalDate.now());
    }

    public boolean isAvailable() {
        return status == CropStatus.AVAILABLE && !isExpired();
    }

    public BigDecimal getTotalValue() {
        return basePrice.multiply(quantity);
    }
}