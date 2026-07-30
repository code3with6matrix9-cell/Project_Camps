package com.vof.dto.request;

import com.vof.constant.PaymentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePaymentStatusRequest {

    @NotNull(message = "Payment status is required.")
    private PaymentStatus paymentStatus;
}