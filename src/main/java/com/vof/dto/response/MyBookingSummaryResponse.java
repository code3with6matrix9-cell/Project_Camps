package com.vof.dto.response;

import com.vof.constant.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyBookingSummaryResponse {
    private String bookingId;
    private Long packageId;
    private String packageName;
    private String thumbnailImage;
    private LocalDate travelDate;
    private BookingStatus bookingStatus;
    private String paymentStatus;
    private int adults;
    private int children;
    private Double totalAmount;
    private LocalDateTime createdAt;
}
