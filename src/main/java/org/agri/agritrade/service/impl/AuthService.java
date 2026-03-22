package org.agri.agritrade.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.agri.agritrade.dto.ResponseStructure;
import org.agri.agritrade.dto.UserDTO;
import org.agri.agritrade.dto.request.LoginRequest;
import org.agri.agritrade.dto.request.RegistrationRequest;
import org.agri.agritrade.dto.response.JwtAuthenticationResponse;
import org.agri.agritrade.entity.User;
import org.agri.agritrade.entity.enums.Role;
import org.agri.agritrade.repository.UserRepository;
import org.agri.agritrade.security.JwtTokenProvider;
import org.agri.agritrade.service.AuthServicePort;
import org.agri.agritrade.service.EmailServicePort;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService implements AuthServicePort {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final EmailServicePort emailService;
    private final SmsService smsService;

    @Override
    @Transactional
    public ResponseStructure<UserDTO> register(RegistrationRequest request) {
        log.info("Attempting to register user with email: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Registration failed - Email already in use: {}", request.getEmail());
            return buildErrorResponse("Email is already in use", HttpStatus.BAD_REQUEST);
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            log.warn("Registration failed - Username already in use: {}", request.getUsername());
            return buildErrorResponse("Username is already in use", HttpStatus.BAD_REQUEST);
        }

        try {
            User user = buildUserFromRequest(request);
            User savedUser = userRepository.save(user);

            log.info("User registered successfully with ID: {}", savedUser.getId());
            UserDTO userDTO = mapToUserDTO(savedUser);

            return buildSuccessResponse("User registered successfully", userDTO, HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Error during user registration: {}", e.getMessage(), e);
            return buildErrorResponse("Registration failed due to server error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseStructure<JwtAuthenticationResponse> login(LoginRequest loginRequest) {
        log.info("Login attempt for user: {}", loginRequest.getEmail());

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = tokenProvider.generateToken(authentication);

            JwtAuthenticationResponse response = new JwtAuthenticationResponse(jwt, "Bearer");

            log.info("User logged in successfully: {}", loginRequest.getEmail());
            return new ResponseStructure<>(HttpStatus.OK.value(), "Login successful", response);

        } catch (BadCredentialsException e) {
            log.warn("Login failed - Invalid credentials for user: {}", loginRequest.getEmail());
            return new ResponseStructure<>(HttpStatus.UNAUTHORIZED.value(), "Invalid username or password", null);
        } catch (AuthenticationException e) {
            log.error("Authentication failed for user: {}", loginRequest.getEmail(), e);
            return new ResponseStructure<>(HttpStatus.UNAUTHORIZED.value(), "Authentication failed", null);
        }
    }

    @Override
    public ResponseStructure<JwtAuthenticationResponse> refreshToken(String refreshToken) {
        log.info("Token refresh attempt");

        if (refreshToken == null || !refreshToken.startsWith("Bearer ")) {
            log.warn("Invalid refresh token format");
            return new ResponseStructure<>(HttpStatus.BAD_REQUEST.value(), "Invalid refresh token", null);
        }

        String token = refreshToken.substring(7);

        if (!tokenProvider.validateToken(token)) {
            log.warn("Invalid refresh token");
            return new ResponseStructure<>(HttpStatus.UNAUTHORIZED.value(), "Invalid refresh token", null);
        }

        String username = tokenProvider.getUsernameFromToken(token);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.getName().equals(username)) {
            log.warn("Refresh token does not match authenticated user");
            return new ResponseStructure<>(HttpStatus.UNAUTHORIZED.value(), "Invalid refresh token", null);
        }

        String newJwt = tokenProvider.generateToken(authentication);
        JwtAuthenticationResponse response = new JwtAuthenticationResponse(newJwt, "Bearer");

        log.info("Token refreshed successfully for user: {}", username);
        return new ResponseStructure<>(HttpStatus.OK.value(), "Token refreshed successfully", response);
    }

    @Override
    public ResponseStructure<Void> logout(String token) {
        log.info("Logout attempt");

        if (token == null || !token.startsWith("Bearer ")) {
            log.warn("Invalid token format for logout");
            return new ResponseStructure<>(HttpStatus.BAD_REQUEST.value(), "Invalid token", null);
        }


        log.info("User logged out successfully");
        return new ResponseStructure<>(HttpStatus.OK.value(), "Logout successful", null);
    }

    @Override
    public ResponseStructure<UserDTO> getCurrentUserProfile() {
        log.info("Fetching current user profile");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("No authenticated user found");
            return buildErrorResponse("User is not authenticated", HttpStatus.UNAUTHORIZED);
        }

        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElse(null);

        if (user == null) {
            log.warn("Authenticated user not found in database: {}", username);
            return buildErrorResponse("User not found", HttpStatus.NOT_FOUND);
        }

        UserDTO userDTO = mapToUserDTO(user);
        log.info("Current user profile fetched successfully for user: {}", username);
        return buildSuccessResponse("User profile fetched successfully", userDTO, HttpStatus.OK);
    }

    @Override
    @Transactional
    public ResponseStructure<Void> forgotPassword(String email, String method) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return new ResponseStructure<>(HttpStatus.NOT_FOUND.value(), "No account found with this email", null);
        }
        String otp = String.format("%06d", new SecureRandom().nextInt(999999));
        user.setOtp(otp);
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(10));
        userRepository.save(user);

        if ("phone".equalsIgnoreCase(method)) {
            if (user.getPhoneNumber() == null || user.getPhoneNumber().isBlank()) {
                return new ResponseStructure<>(HttpStatus.BAD_REQUEST.value(), "No phone number associated with this account", null);
            }
            smsService.sendSms(user.getPhoneNumber(), "Your KrishiSetu OTP is: " + otp + ". Valid for 10 minutes.");
            log.info("OTP sent via SMS to user: {}", email);
            return new ResponseStructure<>(HttpStatus.OK.value(), "OTP sent to your phone number", null);
        } else {
            emailService.sendOtpEmail(email, otp);
            log.info("OTP sent via email to: {}", email);
            return new ResponseStructure<>(HttpStatus.OK.value(), "OTP sent to your email", null);
        }
    }

    @Override
    public ResponseStructure<Void> verifyOtp(String email, String otp) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return new ResponseStructure<>(HttpStatus.NOT_FOUND.value(), "User not found", null);
        }
        if (user.getOtp() == null || !user.getOtp().equals(otp)) {
            return new ResponseStructure<>(HttpStatus.BAD_REQUEST.value(), "Invalid OTP", null);
        }
        if (user.getOtpExpiry() == null || user.getOtpExpiry().isBefore(LocalDateTime.now())) {
            return new ResponseStructure<>(HttpStatus.BAD_REQUEST.value(), "OTP has expired", null);
        }
        return new ResponseStructure<>(HttpStatus.OK.value(), "OTP verified successfully", null);
    }

    @Override
    @Transactional
    public ResponseStructure<Void> resetPassword(String email, String otp, String newPassword) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return new ResponseStructure<>(HttpStatus.NOT_FOUND.value(), "User not found", null);
        }
        if (user.getOtp() == null || !user.getOtp().equals(otp)) {
            return new ResponseStructure<>(HttpStatus.BAD_REQUEST.value(), "Invalid OTP", null);
        }
        if (user.getOtpExpiry() == null || user.getOtpExpiry().isBefore(LocalDateTime.now())) {
            return new ResponseStructure<>(HttpStatus.BAD_REQUEST.value(), "OTP has expired", null);
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setOtp(null);
        user.setOtpExpiry(null);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        return new ResponseStructure<>(HttpStatus.OK.value(), "Password reset successfully", null);
    }

    @Override
    @Transactional
    public ResponseStructure<UserDTO> updateProfile(Map<String, String> updates) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return buildErrorResponse("User is not authenticated", HttpStatus.UNAUTHORIZED);
        }
        User user = userRepository.findByUsername(authentication.getName()).orElse(null);
        if (user == null) {
            return buildErrorResponse("User not found", HttpStatus.NOT_FOUND);
        }
        if (updates.containsKey("fullName")) user.setFullName(updates.get("fullName"));
        if (updates.containsKey("phoneNumber")) user.setPhoneNumber(updates.get("phoneNumber"));
        if (updates.containsKey("address")) user.setAddress(updates.get("address"));
        user.setUpdatedAt(LocalDateTime.now());
        User saved = userRepository.save(user);
        return buildSuccessResponse("Profile updated successfully", mapToUserDTO(saved), HttpStatus.OK);
    }

    @Override
    @Transactional
    public ResponseStructure<Void> changePassword(Map<String, String> request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return new ResponseStructure<>(HttpStatus.UNAUTHORIZED.value(), "Not authenticated", null);
        }
        User user = userRepository.findByUsername(authentication.getName()).orElse(null);
        if (user == null) {
            return new ResponseStructure<>(HttpStatus.NOT_FOUND.value(), "User not found", null);
        }
        String currentPassword = request.get("currentPassword");
        String newPassword = request.get("newPassword");
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            return new ResponseStructure<>(HttpStatus.BAD_REQUEST.value(), "Current password is incorrect", null);
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        return new ResponseStructure<>(HttpStatus.OK.value(), "Password changed successfully", null);
    }

    private User buildUserFromRequest(RegistrationRequest request) {
        User user = new User();
        user.setFullName(request.getFullName());
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setAddress(request.getAddress());
        user.setRole(Role.valueOf(request.getRole().toUpperCase()));
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return user;
    }

    private UserDTO mapToUserDTO(User user) {
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

    private ResponseStructure<UserDTO> buildSuccessResponse(String message, UserDTO data, HttpStatus status) {
        return new ResponseStructure<>(status.value(), message, data);
    }

    private ResponseStructure<UserDTO> buildErrorResponse(String message, HttpStatus status) {
        return new ResponseStructure<>(status.value(), message, null);
    }
}
