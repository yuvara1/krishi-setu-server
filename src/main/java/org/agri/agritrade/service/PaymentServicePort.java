package org.agri.agritrade.service;

import org.agri.agritrade.dto.response.PaymentDTO;
import org.agri.agritrade.dto.response.ResponseStructure;
import org.agri.agritrade.util.enums.PaymentStatus;

public interface PaymentServicePort {
    ResponseStructure<PaymentDTO> createPayment(PaymentDTO dto);
    ResponseStructure<PaymentDTO> getByOrder(Long orderId);
    ResponseStructure<PaymentDTO> updatePaymentStatus(Long paymentId, PaymentStatus status, String transactionId);
}
