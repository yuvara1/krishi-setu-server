package org.agri.agritrade.service;

import org.agri.agritrade.dto.response.ResponseStructure;
import org.agri.agritrade.dto.response.UserDTO;
import org.agri.agritrade.dto.request.LoginRequest;
import org.agri.agritrade.dto.request.RegistrationRequest;
import org.agri.agritrade.dto.response.JwtAuthenticationResponse;

import java.util.Map;

public interface AuthServicePort {
    ResponseStructure<UserDTO> register(RegistrationRequest request);
    ResponseStructure<JwtAuthenticationResponse> login(LoginRequest loginRequest);
    ResponseStructure<JwtAuthenticationResponse> refreshToken(String refreshToken);
    ResponseStructure<Void> logout(String token);
    ResponseStructure<UserDTO> getCurrentUserProfile();
    ResponseStructure<Void> forgotPassword(String email, String method);
    ResponseStructure<Void> verifyOtp(String email, String otp);
    ResponseStructure<Void> resetPassword(String email, String otp, String newPassword);
    ResponseStructure<UserDTO> updateProfile(Map<String, String> updates);
    ResponseStructure<Void> changePassword(Map<String, String> request);
}
