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

import java.util.Map;

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

    @PostMapping("/forgot-password")
    public ResponseEntity<ResponseStructure<Void>> forgotPassword(@RequestBody Map<String, String> body) {
        String method = body.getOrDefault("method", "email");
        ResponseStructure<Void> response = authService.forgotPassword(body.get("email"), method);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<ResponseStructure<Void>> verifyOtp(@RequestBody Map<String, String> body) {
        ResponseStructure<Void> response = authService.verifyOtp(body.get("email"), body.get("otp"));
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ResponseStructure<Void>> resetPassword(@RequestBody Map<String, String> body) {
        ResponseStructure<Void> response = authService.resetPassword(
                body.get("email"), body.get("otp"), body.get("newPassword"));
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PutMapping("/profile")
    public ResponseEntity<ResponseStructure<UserDTO>> updateProfile(@RequestBody Map<String, String> updates) {
        ResponseStructure<UserDTO> response = authService.updateProfile(updates);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PutMapping("/change-password")
    public ResponseEntity<ResponseStructure<Void>> changePassword(@RequestBody Map<String, String> request) {
        ResponseStructure<Void> response = authService.changePassword(request);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }
}