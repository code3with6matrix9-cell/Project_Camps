package com.vof.dto.request;
import jakarta.validation.constraints.*;
import jakarta.validation.Valid;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class CreateBookingRequest {
    @NotBlank(message = "Customer name is required") @Size(max = 100) private String customerName;
    @NotBlank(message = "Email is required") @Email(message = "Invalid email format") @Size(max = 254) private String email;
    @NotBlank(message = "Phone number is required") @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be 10 digits") private String phone;
    @NotNull(message = "Package ID is required") @Positive private Long packageId;
    @NotNull(message = "Travel date is required") @FutureOrPresent(message = "Travel date must be in the present or future") private LocalDate travelDate;
    @NotNull(message = "Number of adults is required") @Min(value = 1, message = "At least one adult is required") private Integer adults;
    @NotNull @Min(value = 0, message = "Children cannot be negative") private Integer children = 0;
    @Size(max = 1000) private String specialRequest;
    private String pickupPoint;
    @Valid
    private List<TravellerRequest> travellers;
}
