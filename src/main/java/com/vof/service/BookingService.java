package com.vof.service;
import com.vof.dto.request.UpdateBookingRequest;
import com.vof.dto.request.CreateBookingRequest;
import com.vof.dto.response.*;

import java.util.List;
public interface BookingService {
    BookingResponse createBooking(CreateBookingRequest request);
    BookingResponse getBookingDetails(String bookingId);
    List<BookingDetailResponse> getAllBookings();
    BookingResponse approveBooking(String bookingId);
    BookingResponse rejectBooking(String bookingId);
    BookingResponse updateBooking(String bookingId, UpdateBookingRequest request);
    void deleteBooking(String bookingId);
    PaymentDetailResponse getPaymentDetails(String bookingId);

    List<com.vof.dto.response.MyBookingSummaryResponse> getMyBookings();
    List<BookingSummaryResponse> getAllBookingSummary();
}
