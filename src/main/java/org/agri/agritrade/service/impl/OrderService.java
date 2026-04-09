package org.agri.agritrade.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.agri.agritrade.dto.response.OrderDTO;
import org.agri.agritrade.dto.response.PagedResponse;
import org.agri.agritrade.dto.response.ResponseStructure;
import org.agri.agritrade.entity.*;
import org.agri.agritrade.util.enums.CropStatus;
import org.agri.agritrade.util.enums.OrderStatus;
import org.agri.agritrade.util.enums.PaymentStatus;
import org.agri.agritrade.repository.*;
import org.agri.agritrade.service.EmailServicePort;
import org.agri.agritrade.service.OrderServicePort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService implements OrderServicePort {

    private final OrderRepository orderRepository;
    private final BidRepository bidRepository;
    private final CropBatchRepository cropBatchRepository;
    private final NotificationService notificationService;
    private final EmailServicePort emailService;

    private OrderDTO toDTO(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setCropBatchId(order.getCropBatch().getId());
        dto.setCropBatchName(order.getCropBatch().getCropName());
        dto.setFarmerId(order.getFarmer().getId());
        dto.setFarmerName(order.getFarmer().getFullName());
        dto.setRetailerId(order.getRetailer().getId());
        dto.setRetailerName(order.getRetailer().getFullName());
        dto.setBidId(order.getBid().getId());
        dto.setFinalAmount(order.getFinalAmount());
        dto.setQuantity(order.getQuantity());
        dto.setOrderStatus(order.getOrderStatus());
        dto.setPaymentStatus(order.getPaymentStatus());
        dto.setDeliveryAddress(order.getDeliveryAddress());
        dto.setOrderDate(order.getOrderDate());
        dto.setDeliveryDate(order.getDeliveryDate());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setUpdatedAt(order.getUpdatedAt());
        return dto;
    }
    @Override
    @Transactional
    public ResponseStructure<OrderDTO> createOrderFromBid(Long bidId, String deliveryAddress) {
        if (deliveryAddress == null || deliveryAddress.trim().isEmpty()) {
            return new ResponseStructure<>(HttpStatus.BAD_REQUEST.value(),
                    "Delivery address is required", null);
        }
        if (orderRepository.findByBid_Id(bidId).isPresent()) {
            return new ResponseStructure<>(HttpStatus.BAD_REQUEST.value(),
                    "Order already exists for this bid", null);
        }

       try {
           Optional<Bid> bidOpt = bidRepository.findById(bidId);
           if (bidOpt.isEmpty())
               return new ResponseStructure<>(HttpStatus.NOT_FOUND.value(), "Bid not found", null);

           Bid bid = bidOpt.get();
           Order order = new Order();
           order.setBid(bid);
           order.setCropBatch(bid.getCropBatch());
           order.setFarmer(bid.getCropBatch().getFarmer());
           order.setRetailer(bid.getRetailer());
           order.setFinalAmount(bid.getBidAmount().multiply(bid.getBidQuantity()));
           order.setQuantity(bid.getBidQuantity());
           order.setOrderStatus(OrderStatus.PENDING);
           order.setPaymentStatus(PaymentStatus.PENDING);
           order.setDeliveryAddress(deliveryAddress);
           order.setOrderDate(LocalDateTime.now());
           order.setCreatedAt(LocalDateTime.now());
           order.setUpdatedAt(LocalDateTime.now());

           bid.getCropBatch().setStatus(CropStatus.SOLD);
           cropBatchRepository.save(bid.getCropBatch());

           Order saved = orderRepository.save(order);

           // Send notifications
           String farmerMsg = String.format("New order created for your crop '%s'. Amount: ₹%s. Check your dashboard.",
                   bid.getCropBatch().getCropName(), order.getFinalAmount());
           String retailerMsg = String.format("Your order for '%s' has been created. Amount: ₹%s. Delivery to: %s",
                   bid.getCropBatch().getCropName(), order.getFinalAmount(), deliveryAddress);

           notificationService.sendNotification(order.getFarmer().getId(), "New Order", farmerMsg, "ORDER_CREATED");
           notificationService.sendNotification(order.getRetailer().getId(), "Order Placed", retailerMsg, "ORDER_CREATED");
           emailService.sendOrderConfirmation(order.getRetailer().getEmail(), retailerMsg);
           emailService.sendOrderConfirmation(order.getFarmer().getEmail(), farmerMsg);

           return new ResponseStructure<>(HttpStatus.CREATED.value(), "Order created", toDTO(saved));
       }
       catch (Exception e) {
           log.error("Error creating order from bid {}: {}", bidId, e.getMessage(), e);
           return new ResponseStructure<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to create order", null);
       }

    }
    @Override
    public ResponseStructure<OrderDTO> getById(Long id) {
        return orderRepository.findById(id)
                .map(o -> new ResponseStructure<>(HttpStatus.OK.value(), "Order found", toDTO(o)))
                .orElse(new ResponseStructure<>(HttpStatus.NOT_FOUND.value(), "Order not found", null));
    }
    @Override
    public ResponseStructure<List<OrderDTO>> getByFarmer(Long farmerId) {
        List<OrderDTO> orders = orderRepository.findByFarmer_Id(farmerId).stream().map(this::toDTO).toList();
        return new ResponseStructure<>(HttpStatus.OK.value(), "Orders retrieved", orders);
    }
    @Override
    public ResponseStructure<List<OrderDTO>> getByRetailer(Long retailerId) {
        List<OrderDTO> orders = orderRepository.findByRetailer_Id(retailerId).stream().map(this::toDTO).toList();
        return new ResponseStructure<>(HttpStatus.OK.value(), "Orders retrieved", orders);
    }
    @Override
    public ResponseStructure<List<OrderDTO>> getAll() {
        List<OrderDTO> orders = orderRepository.findAll().stream().map(this::toDTO).toList();
        return new ResponseStructure<>(HttpStatus.OK.value(), "Orders retrieved", orders);
    }
    @Override
    public ResponseStructure<PagedResponse<OrderDTO>> getAllPaged(int page, int size) {
        Page<Order> orderPage = orderRepository.findAll(
                org.springframework.data.domain.PageRequest.of(page, size,
                        org.springframework.data.domain.Sort.by("createdAt").descending()));
        List<OrderDTO> dtos = orderPage.getContent().stream().map(this::toDTO).toList();
        PagedResponse<OrderDTO> paged = new PagedResponse<>(
                dtos, orderPage.getNumber(), orderPage.getSize(),
                orderPage.getTotalElements(), orderPage.getTotalPages(), orderPage.isLast());
        return new ResponseStructure<>(HttpStatus.OK.value(), "Orders retrieved", paged);
    }
    @Override
    public ResponseStructure<PagedResponse<OrderDTO>> getByFarmerPaged(Long farmerId, int page, int size) {
        Page<Order> orderPage = orderRepository.findByFarmer_Id(farmerId,
                org.springframework.data.domain.PageRequest.of(page, size,
                        org.springframework.data.domain.Sort.by("createdAt").descending()));
        List<OrderDTO> dtos = orderPage.getContent().stream().map(this::toDTO).toList();
        PagedResponse<OrderDTO> paged = new PagedResponse<>(
                dtos, orderPage.getNumber(), orderPage.getSize(),
                orderPage.getTotalElements(), orderPage.getTotalPages(), orderPage.isLast());
        return new ResponseStructure<>(HttpStatus.OK.value(), "Orders retrieved", paged);
    }

    public ResponseStructure<PagedResponse<OrderDTO>> getByRetailerPaged(Long retailerId, int page, int size) {
        Page<Order> orderPage = orderRepository.findByRetailer_Id(retailerId,
                org.springframework.data.domain.PageRequest.of(page, size,
                        org.springframework.data.domain.Sort.by("createdAt").descending()));
        List<OrderDTO> dtos = orderPage.getContent().stream().map(this::toDTO).toList();
        PagedResponse<OrderDTO> paged = new PagedResponse<>(
                dtos, orderPage.getNumber(), orderPage.getSize(),
                orderPage.getTotalElements(), orderPage.getTotalPages(), orderPage.isLast());
        return new ResponseStructure<>(HttpStatus.OK.value(), "Orders retrieved", paged);
    }
    @Override
    @Transactional
    public ResponseStructure<OrderDTO> updateStatus(Long id, OrderStatus status) {
        Optional<Order> orderOpt = orderRepository.findById(id);
        if (orderOpt.isEmpty())
            return new ResponseStructure<>(HttpStatus.NOT_FOUND.value(), "Order not found", null);
        Order order = orderOpt.get();
        order.setOrderStatus(status);
        order.setUpdatedAt(LocalDateTime.now());
        if (status == OrderStatus.DELIVERED) order.setDeliveryDate(LocalDateTime.now());
        Order saved = orderRepository.save(order);

        // Notify both parties about status update
        String msg = String.format("Order #%d for '%s' status updated to %s",
                order.getId(), order.getCropBatch().getCropName(), status);
        notificationService.sendNotification(order.getFarmer().getId(), "Order Update", msg, "ORDER_STATUS");
        notificationService.sendNotification(order.getRetailer().getId(), "Order Update", msg, "ORDER_STATUS");

        return new ResponseStructure<>(HttpStatus.OK.value(), "Status updated", toDTO(saved));
    }
}