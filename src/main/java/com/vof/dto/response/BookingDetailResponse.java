package com.vof.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class BookingDetailResponse {

    private Long id;
    private String bookingId;

    // Customer information
    private String customerName;
    private String email;
    private String phone;

    // Booking information
    private Long packageId;
    private String packageName;
    private int adults;
    private int children;
    private LocalDate travelDate;
    private String specialRequest;
    private String pickupPoint;
    private String status;
    private Double totalAmount;
    private LocalDateTime createdAt;

    // Payment information
    private String paymentStatus;
    private String utrNumber;
    private String paymentProofUrl;
    private LocalDateTime paymentUploadedAt;

    // Traveller information
    private List<TravellerResponse> travellers;
}