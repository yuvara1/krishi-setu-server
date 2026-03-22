package org.agri.agritrade.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.agri.agritrade.dto.ResponseStructure;
import org.agri.agritrade.dto.UserDTO;
import org.agri.agritrade.entity.User;
import org.agri.agritrade.entity.enums.Role;
import org.agri.agritrade.repository.UserRepository;
import org.agri.agritrade.service.UserServicePort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.agri.agritrade.dto.PagedResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService implements UserServicePort {

    private final UserRepository userRepository;

    private UserDTO toDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setAddress(user.getAddress());
        dto.setRole(user.getRole().name());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        return dto;
    }
@Override
    public ResponseStructure<List<UserDTO>> getAllUsers() {
        try {
            List<UserDTO> users = userRepository.findAll().stream().map(this::toDTO).toList();
            return new ResponseStructure<>(HttpStatus.OK.value(), "Users retrieved successfully", users);
        } catch (Exception e) {
            log.error("Error retrieving users: {}", e.getMessage(), e);
            return new ResponseStructure<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to retrieve users", null);
        }
    }
    @Override
    public ResponseStructure<PagedResponse<UserDTO>> getAllUsersPaged(int page, int size) {
        try {
            Page<User> userPage = userRepository.findAll(
                    PageRequest.of(page, size, Sort.by("createdAt").descending()));
            List<UserDTO> dtos = userPage.getContent().stream()
                    .map(this::toDTO).toList();
            PagedResponse<UserDTO> paged = new PagedResponse<>(
                    dtos, userPage.getNumber(), userPage.getSize(),
                    userPage.getTotalElements(), userPage.getTotalPages(), userPage.isLast());
            return new ResponseStructure<>(HttpStatus.OK.value(), "Users retrieved successfully", paged);
        } catch (Exception e) {
            log.error("Error retrieving paged users: {}", e.getMessage(), e);
            return new ResponseStructure<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to retrieve users", null);
        }
    }

    @Override
    public ResponseStructure<UserDTO> getUserById(Long id) {
        try {
            return userRepository.findById(id)
                    .map(u -> new ResponseStructure<>(HttpStatus.OK.value(), "User found", toDTO(u)))
                    .orElse(new ResponseStructure<>(HttpStatus.NOT_FOUND.value(), "User not found", null));
        } catch (Exception e) {
            log.error("Error retrieving user with ID {}: {}", id, e.getMessage(), e);
            return new ResponseStructure<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to retrieve user", null);
        }
    }
    @Override
    @Transactional
    public ResponseStructure<UserDTO> updateUser(Long id, UserDTO dto) {
        try {
            Optional<User> userOpt = userRepository.findById(id);
            if (userOpt.isEmpty()) {
                return new ResponseStructure<>(HttpStatus.NOT_FOUND.value(), "User not found", null);
            }

            User user = userOpt.get();

            // Update only non-null fields
            if (dto.getFullName() != null && !dto.getFullName().trim().isEmpty()) {
                user.setFullName(dto.getFullName());
            }
            if (dto.getEmail() != null && !dto.getEmail().trim().isEmpty()) {
                // Check if email is already taken by another user
                if (userRepository.existsByEmailAndIdNot(dto.getEmail(), id)) {
                    return new ResponseStructure<>(HttpStatus.BAD_REQUEST.value(), "Email is already in use", null);
                }
                user.setEmail(dto.getEmail());
            }
            if (dto.getPhoneNumber() != null && !dto.getPhoneNumber().trim().isEmpty()) {
                user.setPhoneNumber(dto.getPhoneNumber());
            }
            if (dto.getAddress() != null && !dto.getAddress().trim().isEmpty()) {
                user.setAddress(dto.getAddress());
            }
            if (dto.getRole() != null && !dto.getRole().trim().isEmpty()) {
                try {
                    user.setRole(Role.valueOf(dto.getRole().toUpperCase()));
                } catch (IllegalArgumentException e) {
                    return new ResponseStructure<>(HttpStatus.BAD_REQUEST.value(), "Invalid role specified", null);
                }
            }

            user.setUpdatedAt(LocalDateTime.now());
            User savedUser = userRepository.save(user);

            log.info("User updated successfully with ID: {}", id);
            return new ResponseStructure<>(HttpStatus.OK.value(), "User updated successfully", toDTO(savedUser));
        } catch (Exception e) {
            log.error("Error updating user with ID {}: {}", id, e.getMessage(), e);
            return new ResponseStructure<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to update user", null);
        }
    }
    @Override
    @Transactional
    public ResponseStructure<Void> deleteUser(Long id) {
        try {
            if (!userRepository.existsById(id)) {
                return new ResponseStructure<>(HttpStatus.NOT_FOUND.value(), "User not found", null);
            }

            userRepository.deleteById(id);
            log.info("User deleted successfully with ID: {}", id);
            return new ResponseStructure<>(HttpStatus.NO_CONTENT.value(), "User deleted successfully", null);
        } catch (Exception e) {
            log.error("Error deleting user with ID {}: {}", id, e.getMessage(), e);
            return new ResponseStructure<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to delete user", null);
        }
    }
}
