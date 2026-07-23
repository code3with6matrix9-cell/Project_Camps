package com.vof.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegistrationRequest {
    @NotBlank @Email @Size(max = 254)
    private String email;

    @NotBlank @Size(min = 6, max = 18)
    private String password;

    @NotBlank(message = "Mobile number is required")
    @Pattern(
            regexp = "^[6-9]\\d{9}$",
            message = "Enter a valid 10-digit mobile number"
    )
    private String mobileNumber;
}
