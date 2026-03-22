package org.agri.agritrade.service;

import org.agri.agritrade.dto.PaymentDTO;
import org.agri.agritrade.dto.ResponseStructure;
import org.agri.agritrade.entity.enums.PaymentStatus;

public interface PaymentServicePort {
    ResponseStructure<PaymentDTO> createPayment(PaymentDTO dto);
    ResponseStructure<PaymentDTO> getByOrder(Long orderId);
    ResponseStructure<PaymentDTO> updatePaymentStatus(Long paymentId, PaymentStatus status, String transactionId);
}
