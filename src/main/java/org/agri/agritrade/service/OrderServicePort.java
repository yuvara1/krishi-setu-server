package org.agri.agritrade.service;

import org.agri.agritrade.dto.response.OrderDTO;
import org.agri.agritrade.dto.response.PagedResponse;
import org.agri.agritrade.dto.response.ResponseStructure;
import org.agri.agritrade.util.enums.OrderStatus;

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
