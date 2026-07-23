package com.vof.controller;
import com.vof.dto.response.BookingDetailResponse;
import com.vof.dto.response.BookingResponse;
import com.vof.dto.response.CommonApiResponse;
import com.vof.service.BookingService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequestMapping("/api/admin/bookings") @RequiredArgsConstructor @SecurityRequirement(name = "bearerAuth")
public class AdminBookingController {
    private final BookingService bookingService;
    @GetMapping
    public ResponseEntity<CommonApiResponse> getAllBookings() {
        List<BookingDetailResponse> bookings = bookingService.getAllBookings();
        return ResponseEntity.ok(CommonApiResponse.builder().success(true).message("All bookings retrieved.").data(bookings).build());
    }
    @PutMapping("/{id}/approve")
    public ResponseEntity<CommonApiResponse> approveBooking(@PathVariable Long id) {
        return ResponseEntity.ok(CommonApiResponse.builder().success(true).data(bookingService.approveBooking(id)).message("Booking approved successfully.").build());
    }
    @PutMapping("/{id}/reject")
    public ResponseEntity<CommonApiResponse> rejectBooking(@PathVariable Long id) {
        return ResponseEntity.ok(CommonApiResponse.builder().success(true).data(bookingService.rejectBooking(id)).message("Booking rejected successfully.").build());
    }
}
