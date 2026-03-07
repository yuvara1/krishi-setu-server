package org.agri.agritrade.controller;

import lombok.RequiredArgsConstructor;
import org.agri.agritrade.dto.OrderDTO;
import org.agri.agritrade.dto.ResponseStructure;
import org.agri.agritrade.entity.enums.OrderStatus;
import org.agri.agritrade.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/from-bid/{bidId}")
    public ResponseEntity<ResponseStructure<OrderDTO>> createFromBid(
            @PathVariable Long bidId,
            @RequestBody(required = false) Map<String, String> body) {
        String address = body != null ? body.getOrDefault("deliveryAddress", "") : "";
        ResponseStructure<OrderDTO> res = orderService.createOrderFromBid(bidId, address);
        return ResponseEntity.status(res.getStatusCode()).body(res);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseStructure<OrderDTO>> getById(@PathVariable Long id) {
        ResponseStructure<OrderDTO> res = orderService.getById(id);
        return ResponseEntity.status(res.getStatusCode()).body(res);
    }

    @GetMapping("/farmer/{farmerId}")
    public ResponseEntity<ResponseStructure<List<OrderDTO>>> getByFarmer(@PathVariable Long farmerId) {
        ResponseStructure<List<OrderDTO>> res = orderService.getByFarmer(farmerId);
        return ResponseEntity.status(res.getStatusCode()).body(res);
    }

    @GetMapping("/retailer/{retailerId}")
    public ResponseEntity<ResponseStructure<List<OrderDTO>>> getByRetailer(@PathVariable Long retailerId) {
        ResponseStructure<List<OrderDTO>> res = orderService.getByRetailer(retailerId);
        return ResponseEntity.status(res.getStatusCode()).body(res);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseStructure<List<OrderDTO>>> getAll() {
        ResponseStructure<List<OrderDTO>> res = orderService.getAll();
        return ResponseEntity.status(res.getStatusCode()).body(res);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseStructure<OrderDTO>> updateStatus(
            @PathVariable Long id, @RequestParam String status) {
        try {
            OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
            ResponseStructure<OrderDTO> res = orderService.updateStatus(id, orderStatus);
            return ResponseEntity.status(res.getStatusCode()).body(res);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                new ResponseStructure<>(400, "Invalid order status: " + status, null));
        }
    }
}