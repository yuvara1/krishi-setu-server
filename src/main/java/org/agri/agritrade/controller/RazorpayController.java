package org.agri.agritrade.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.agri.agritrade.dto.response.ResponseStructure;
import org.agri.agritrade.dto.request.PaymentVerificationRequest;
import org.agri.agritrade.dto.response.RazorpayOrderResponse;
import org.agri.agritrade.service.impl.RazorpayService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments/razorpay")
@RequiredArgsConstructor
public class RazorpayController {

    private final RazorpayService razorpayService;

    @PostMapping("/create-order/{bidId}")
    public ResponseEntity<ResponseStructure<RazorpayOrderResponse>> createOrder(@PathVariable Long bidId) {
        ResponseStructure<RazorpayOrderResponse> res = razorpayService.createOrder(bidId);
        return ResponseEntity.status(res.getStatusCode()).body(res);
    }

    @PostMapping("/verify")
    public ResponseEntity<ResponseStructure<String>> verifyPayment(
            @Valid @RequestBody PaymentVerificationRequest request) {
        ResponseStructure<String> res = razorpayService.verifyAndProcessPayment(request);
        return ResponseEntity.status(res.getStatusCode()).body(res);
    }
}