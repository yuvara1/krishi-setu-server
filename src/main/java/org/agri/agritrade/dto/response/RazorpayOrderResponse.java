package org.agri.agritrade.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RazorpayOrderResponse {
    private String razorpayOrderId;
    private BigDecimal amount;
    private String currency;
    private String key;
    private Long bidId;
    private String retailerName;
    private String retailerPhone;
    private String retailerEmail;
    private String cropName;
}
