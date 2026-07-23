package com.vof.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PasswordResetRequest {
    @NotBlank @Email @Size(max = 254)
    private String email;

    @NotBlank @Pattern(regexp = "^\\d{6}$", message = "OTP must be a six-digit code")
    private String otp;

    @NotBlank @Size(min = 12, max = 72)
    private String newPassword;
}
