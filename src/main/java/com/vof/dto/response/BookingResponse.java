package com.vof.dto.response;
import lombok.Builder;
import lombok.Data;
@Data @Builder
public class BookingResponse {
    private String bookingId;
    private String status;
    private Double totalAmount;
    private String message;
}
