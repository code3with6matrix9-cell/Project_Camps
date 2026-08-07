package com.vof.controller;
import com.vof.dto.request.UpdateBookingRequest;
import com.vof.dto.request.CreateBookingRequest;
import com.vof.dto.response.BookingResponse;
import com.vof.dto.response.CommonApiResponse;
import com.vof.service.BookingService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.vof.dto.response.MyBookingSummaryResponse;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class BookingController {
    private final BookingService bookingService;

    @GetMapping
    public ResponseEntity<CommonApiResponse> getMyBookings() {
        List<MyBookingSummaryResponse> bookings = bookingService.getMyBookings();
        return ResponseEntity.ok(
                CommonApiResponse.builder()
                        .success(true)
                        .message("User bookings retrieved successfully")
                        .data(bookings)
                        .build()
        );
    }

    @PostMapping
    public ResponseEntity<CommonApiResponse> createBooking(@Valid @RequestBody CreateBookingRequest request) {
        BookingResponse bookingResponse = bookingService.createBooking(request);
        return new ResponseEntity<>(CommonApiResponse.builder()
                .success(true)
                .message(bookingResponse.getMessage())
                .data(bookingResponse)
                .build(), HttpStatus.CREATED);
    }
    @GetMapping("/{bookingId}")
    public ResponseEntity<CommonApiResponse> getBookingStatus(@PathVariable String bookingId) {
        BookingResponse bookingResponse = bookingService.getBookingDetails(bookingId);
        return ResponseEntity.ok(CommonApiResponse.builder().success(true).message("Booking status retrieved successfully").data(bookingResponse).build());
    }

    @PutMapping("/{bookingId}")
    public ResponseEntity<CommonApiResponse> updateBooking(@PathVariable String bookingId, @Valid @RequestBody UpdateBookingRequest request) {
        BookingResponse bookingResponse = bookingService.updateBooking(bookingId, request);
        return ResponseEntity.ok(CommonApiResponse.builder().success(true).message("Booking updated successfully.").data(bookingResponse).build());
    }
}
