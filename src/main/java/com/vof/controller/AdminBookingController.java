package com.vof.controller;

import com.vof.dto.request.UpdatePaymentStatusRequest;
import com.vof.dto.response.BookingDetailResponse;
import com.vof.dto.response.CommonApiResponse;
import com.vof.dto.response.PaymentDetailResponse;
import com.vof.service.BookingService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/bookings")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class AdminBookingController {

    private final BookingService bookingService;

    @GetMapping
    public ResponseEntity<CommonApiResponse> getAllBookings() {
        List<BookingDetailResponse> bookings = bookingService.getAllBookings();
        return ResponseEntity.ok(CommonApiResponse.builder().success(true).message("All bookings retrieved.").data(bookings).build());
    }

    @PutMapping("/{bookingId}/approve")
    public ResponseEntity<CommonApiResponse> approveBooking(@PathVariable String bookingId) {
        return ResponseEntity.ok(CommonApiResponse.builder().success(true).data(bookingService.approveBooking(bookingId)).message("Booking approved successfully.").build());
    }

    @PutMapping("/{bookingId}/reject")
    public ResponseEntity<CommonApiResponse> rejectBooking(@PathVariable String bookingId) {
        return ResponseEntity.ok(CommonApiResponse.builder().success(true).data(bookingService.rejectBooking(bookingId)).message("Booking rejected successfully.").build());
    }

    @DeleteMapping("/{bookingId}")
    public ResponseEntity<CommonApiResponse> deleteBooking(@PathVariable String bookingId) {
        bookingService.deleteBooking(bookingId);
        return ResponseEntity.ok(CommonApiResponse.builder().success(true).message("Booking deleted successfully.").build());
    }

    @GetMapping("/{bookingId}/payment")
    public ResponseEntity<CommonApiResponse> getPaymentDetails(@PathVariable String bookingId) {
        return ResponseEntity.ok(CommonApiResponse.builder().success(true).data(bookingService.getPaymentDetails(bookingId)).message("Payment details retrieved successfully.").build());
    }

    @PutMapping("/{bookingId}/payment-status")
    public ResponseEntity<CommonApiResponse> updatePaymentStatus(
            @PathVariable String bookingId,
            @Valid @RequestBody UpdatePaymentStatusRequest request) {

        PaymentDetailResponse response =
                bookingService.updatePaymentStatus(bookingId, request);

        return ResponseEntity.ok(
                CommonApiResponse.builder()
                        .success(true)
                        .message("Payment status updated successfully.")
                        .data(response)
                        .build()
        );
    }
}