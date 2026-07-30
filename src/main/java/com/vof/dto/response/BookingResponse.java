package com.vof.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class BookingResponse {
    private String bookingId;
    private String message;

    // Booking information
    private String bookingStatus;
    private String paymentStatus;
    private String packageName;
    private Long packageId;
    private LocalDate travelDate;
    private LocalDateTime bookingDate;
    private Double totalAmount;
    private int adults;
    private int children;
    private String specialRequest;
    private String pickupPoint;

    // Customer information
    private String customerName;
    private String email;
    private String phone;

    // Payment information
    private String utrNumber;
    private String paymentProofUrl;

    // Traveller information
    private List<TravellerResponse> travellers;
}
