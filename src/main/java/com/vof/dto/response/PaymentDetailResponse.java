package com.vof.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PaymentDetailResponse {
    private String bookingId;
    private String paymentStatus;
    private Double amount; // Assuming the amount is part of the payment proof or booking
    private String utrNumber;
    private String screenshotUrl;
    private LocalDateTime uploadedAt;
}
