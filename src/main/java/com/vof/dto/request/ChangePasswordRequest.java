package com.vof.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChangePasswordRequest {
    @NotBlank @Pattern(regexp = "^\\d{6}$", message = "OTP must be a six-digit code")
    private String otp;

    @NotBlank @Size(min = 12, max = 72)
    private String newPassword;
}
