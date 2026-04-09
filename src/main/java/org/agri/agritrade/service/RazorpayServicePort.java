package org.agri.agritrade.service;

import org.agri.agritrade.dto.response.ResponseStructure;
import org.agri.agritrade.dto.request.PaymentVerificationRequest;
import org.agri.agritrade.dto.response.RazorpayOrderResponse;

public interface RazorpayServicePort {
    ResponseStructure<RazorpayOrderResponse> createOrder(Long bidId);
    ResponseStructure<String> verifyAndProcessPayment(PaymentVerificationRequest request);
}
