package com.vof.dto.request;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
@Data
public class ContactRequest {
    @NotBlank(message = "Name is required") @Size(max = 100) private String name;
    @NotBlank(message = "Email is required") @Email(message = "Invalid email format") @Size(max = 254) private String email;
    @NotBlank(message = "Phone is required") @Pattern(regexp = "^[0-9+() -]{7,25}$", message = "Invalid phone number") private String phone;
    @NotBlank(message = "Message is required") @Size(max = 2000) private String message;
}
