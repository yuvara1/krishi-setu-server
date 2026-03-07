package org.agri.agritrade.dto;

import lombok.Data;
import org.agri.agritrade.entity.enums.OrderStatus;
import org.agri.agritrade.entity.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderDTO {
    private Long id;
    private Long cropBatchId;
    private String cropBatchName;
    private Long farmerId;
    private String farmerName;
    private Long retailerId;
    private String retailerName;
    private Long bidId;
    private BigDecimal finalAmount;
    private BigDecimal quantity;
    private OrderStatus orderStatus;
    private PaymentStatus paymentStatus;
    private String deliveryAddress;
    private LocalDateTime orderDate;
    private LocalDateTime deliveryDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}