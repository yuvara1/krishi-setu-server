package org.agri.agritrade.controller;

import lombok.RequiredArgsConstructor;
import org.agri.agritrade.dto.PaymentDTO;
import org.agri.agritrade.dto.ResponseStructure;
import org.agri.agritrade.entity.enums.PaymentStatus;
import org.agri.agritrade.service.impl.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseStructure<PaymentDTO>> create(@RequestBody PaymentDTO dto) {
        ResponseStructure<PaymentDTO> res = paymentService.createPayment(dto);
        return ResponseEntity.status(res.getStatusCode()).body(res);
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<ResponseStructure<PaymentDTO>> getByOrder(@PathVariable Long orderId) {
        ResponseStructure<PaymentDTO> res = paymentService.getByOrder(orderId);
        return ResponseEntity.status(res.getStatusCode()).body(res);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseStructure<PaymentDTO>> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        PaymentStatus status = PaymentStatus.valueOf(body.get("status").toUpperCase());
        String txId = body.get("transactionId");
        ResponseStructure<PaymentDTO> res = paymentService.updatePaymentStatus(id, status, txId);
        return ResponseEntity.status(res.getStatusCode()).body(res);
    }
}