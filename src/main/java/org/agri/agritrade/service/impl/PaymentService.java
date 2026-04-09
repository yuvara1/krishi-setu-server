package org.agri.agritrade.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.agri.agritrade.dto.response.PaymentDTO;
import org.agri.agritrade.dto.response.ResponseStructure;
import org.agri.agritrade.entity.Order;
import org.agri.agritrade.entity.Payment;
import org.agri.agritrade.util.enums.PaymentStatus;
import org.agri.agritrade.repository.OrderRepository;
import org.agri.agritrade.repository.PaymentRepository;
import org.agri.agritrade.service.PaymentServicePort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService implements PaymentServicePort {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    private PaymentDTO toDTO(Payment payment) {
        PaymentDTO dto = new PaymentDTO();
        dto.setId(payment.getId());
        dto.setOrderId(payment.getOrder().getId());
        dto.setAmount(payment.getAmount());
        dto.setPaymentMethod(payment.getPaymentMethod());
        dto.setPaymentStatus(payment.getPaymentStatus());
        dto.setTransactionId(payment.getTransactionId());
        dto.setPaymentDate(payment.getPaymentDate());
        dto.setCreatedAt(payment.getCreatedAt());
        return dto;
    }
    @Override
    @Transactional
    public ResponseStructure<PaymentDTO> createPayment(PaymentDTO dto) {
        Optional<Order> orderOpt = orderRepository.findById(dto.getOrderId());
        if (orderOpt.isEmpty())
            return new ResponseStructure<>(HttpStatus.NOT_FOUND.value(), "Order not found", null);

        Payment payment = new Payment();
        payment.setOrder(orderOpt.get());
        payment.setAmount(dto.getAmount());
        payment.setPaymentMethod(dto.getPaymentMethod());
        payment.setPaymentStatus(PaymentStatus.PENDING);
        payment.setCreatedAt(LocalDateTime.now());

        Payment saved = paymentRepository.save(payment);
        return new ResponseStructure<>(HttpStatus.CREATED.value(), "Payment created", toDTO(saved));
    }
    @Override
    public ResponseStructure<PaymentDTO> getByOrder(Long orderId) {
        return paymentRepository.findByOrder_Id(orderId)
                .map(p -> new ResponseStructure<>(HttpStatus.OK.value(), "Payment found", toDTO(p)))
                .orElse(new ResponseStructure<>(HttpStatus.NOT_FOUND.value(), "Payment not found", null));
    }
    @Override
    @Transactional
    public ResponseStructure<PaymentDTO> updatePaymentStatus(Long paymentId, PaymentStatus status, String transactionId) {
        Optional<Payment> payOpt = paymentRepository.findById(paymentId);
        if (payOpt.isEmpty())
            return new ResponseStructure<>(HttpStatus.NOT_FOUND.value(), "Payment not found", null);

        Payment payment = payOpt.get();
        payment.setPaymentStatus(status);
        if (transactionId != null) payment.setTransactionId(transactionId);
        if (status == PaymentStatus.COMPLETED) {
            payment.setPaymentDate(LocalDateTime.now());
            // Also update order payment status
            Order order = payment.getOrder();
            order.setPaymentStatus(PaymentStatus.COMPLETED);
            orderRepository.save(order);
        }
        return new ResponseStructure<>(HttpStatus.OK.value(), "Payment updated", toDTO(paymentRepository.save(payment)));
    }
}