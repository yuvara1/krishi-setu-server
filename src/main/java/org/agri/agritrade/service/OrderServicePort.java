package org.agri.agritrade.service;

import org.agri.agritrade.dto.OrderDTO;
import org.agri.agritrade.dto.PagedResponse;
import org.agri.agritrade.dto.ResponseStructure;
import org.agri.agritrade.entity.enums.OrderStatus;

import java.util.List;

public interface OrderServicePort {
    ResponseStructure<OrderDTO> createOrderFromBid(Long bidId, String deliveryAddress);
    ResponseStructure<OrderDTO> getById(Long id);
    ResponseStructure<java.util.List<OrderDTO>> getByFarmer(Long farmerId);
    ResponseStructure<java.util.List<OrderDTO>> getByRetailer(Long retailerId);
    ResponseStructure<java.util.List<OrderDTO>> getAll();
    ResponseStructure<PagedResponse<OrderDTO>> getAllPaged(int page, int size);
    ResponseStructure<PagedResponse<OrderDTO>> getByFarmerPaged(Long farmerId, int page, int size);
    ResponseStructure<PagedResponse<OrderDTO>> getByRetailerPaged(Long retailerId, int page, int size);
    ResponseStructure<OrderDTO> updateStatus(Long id, OrderStatus status);
}
