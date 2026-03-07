package org.agri.agritrade.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.agri.agritrade.dto.ResponseStructure;
import org.agri.agritrade.dto.UserDTO;
import org.agri.agritrade.dto.request.LoginRequest;
import org.agri.agritrade.dto.request.RegistrationRequest;
import org.agri.agritrade.dto.response.JwtAuthenticationResponse;
import org.agri.agritrade.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ResponseStructure<UserDTO>> registerUser(@Valid @RequestBody RegistrationRequest request) {
        log.info("Received registration request for email: {}", request.getEmail());
        ResponseStructure<UserDTO> response = authService.register(request);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<ResponseStructure<JwtAuthenticationResponse>> loginUser(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("Received login request for user: {}", loginRequest.getEmail());
        ResponseStructure<JwtAuthenticationResponse> response = authService.login(loginRequest);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    // refresh token
        @PostMapping("/refresh")
    public ResponseEntity<ResponseStructure<JwtAuthenticationResponse>> refreshToken(@RequestHeader("Authorization") String refreshToken) {
        log.info("Received token refresh request");
        ResponseStructure<JwtAuthenticationResponse> response = authService.refreshToken(refreshToken);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    // logout
    @PostMapping("/logout")
    public ResponseEntity<ResponseStructure<Void>> logoutUser(@RequestHeader("Authorization") String token) {
        log.info("Received logout request");
        ResponseStructure<Void> response = authService.logout(token);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    // current user profile
    @GetMapping("/me")
    public ResponseEntity<ResponseStructure<UserDTO>> getCurrentUserProfile() {
        log.info("Received request for current user profile");
        ResponseStructure<UserDTO> response = authService.getCurrentUserProfile();
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }
}