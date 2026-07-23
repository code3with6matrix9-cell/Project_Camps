package com.vof.dto.request;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
@Data
public class PaymentProofRequest {
    @NotBlank(message = "Booking ID is required") @Size(max = 64) private String bookingId;
    @NotBlank(message = "UTR number is required") @Size(min = 6, max = 64) private String utrNumber;
    @NotNull(message = "Amount is required") @DecimalMin(value = "0.01") private Double amount;
    private MultipartFile screenshot;
}
