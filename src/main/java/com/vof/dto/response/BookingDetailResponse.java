package com.vof.dto.response;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
@Data @Builder
public class BookingDetailResponse {
    private Long id;
    private String bookingId;
    private String customerName;
    private String email;
    private String phone;
    private int adults;
    private int children;
    private LocalDate travelDate;
    private String specialRequest;
    private String status;
    private LocalDateTime createdAt;
    private String packageName;
}
