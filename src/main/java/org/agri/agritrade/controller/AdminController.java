package org.agri.agritrade.controller;

import lombok.RequiredArgsConstructor;
import org.agri.agritrade.dto.response.PagedResponse;
import org.agri.agritrade.dto.response.ResponseStructure;
import org.agri.agritrade.dto.response.UserDTO;
import org.agri.agritrade.service.impl.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;

    @GetMapping("/users")
    public ResponseEntity<ResponseStructure<List<UserDTO>>> getAllUsers() {
        ResponseStructure<List<UserDTO>> res = userService.getAllUsers();
        return ResponseEntity.status(res.getStatusCode()).body(res);
    }

    @GetMapping("/users/paged")
    public ResponseEntity<ResponseStructure<PagedResponse<UserDTO>>> getAllUsersPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        ResponseStructure<PagedResponse<UserDTO>> res = userService.getAllUsersPaged(page, size);
        return ResponseEntity.status(res.getStatusCode()).body(res);
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<ResponseStructure<UserDTO>> getUserById(@PathVariable Long id) {
        ResponseStructure<UserDTO> res = userService.getUserById(id);
        return ResponseEntity.status(res.getStatusCode()).body(res);
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<ResponseStructure<UserDTO>> updateUser(
            @PathVariable Long id, @RequestBody UserDTO dto) {
        ResponseStructure<UserDTO> res = userService.updateUser(id, dto);
        return ResponseEntity.status(res.getStatusCode()).body(res);
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<ResponseStructure<Void>> deleteUser(@PathVariable Long id) {
        ResponseStructure<Void> res = userService.deleteUser(id);
        return ResponseEntity.status(res.getStatusCode()).body(res);
    }
}