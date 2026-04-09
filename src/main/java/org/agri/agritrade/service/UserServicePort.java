package org.agri.agritrade.service;

import org.agri.agritrade.dto.response.PagedResponse;
import org.agri.agritrade.dto.response.ResponseStructure;
import org.agri.agritrade.dto.response.UserDTO;

import java.util.List;

public interface UserServicePort {
    ResponseStructure<List<UserDTO>> getAllUsers();
    ResponseStructure<PagedResponse<UserDTO>> getAllUsersPaged(int page, int size);
    ResponseStructure<UserDTO> getUserById(Long id);
    ResponseStructure<UserDTO> updateUser(Long id, UserDTO dto);
    ResponseStructure<Void> deleteUser(Long id);
}
