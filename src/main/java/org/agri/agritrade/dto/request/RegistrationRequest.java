package org.agri.agritrade.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class RegistrationRequest {
    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Password is required")
    private String password;

    @NotBlank(message = "Phone number is required")
    private String phoneNumber;

    @Email(message = "Email should be valid")
    @NotBlank(message = "Email is required")
    private String email;

    private String address;

    @NotBlank(message = "Role is required")
    @Pattern(regexp = "FARMER|RETAILER|ADMIN", message = "Role must be FARMER, RETAILER, or ADMIN")
    private String role;
}
