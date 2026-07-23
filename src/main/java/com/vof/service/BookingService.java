package com.vof.service;
import com.vof.dto.request.CreateBookingRequest;
import com.vof.dto.response.BookingDetailResponse;
import com.vof.dto.response.BookingResponse;
import java.util.List;
public interface BookingService {
    BookingResponse createBooking(CreateBookingRequest request);
    BookingResponse getBookingStatus(String bookingId);
    List<BookingDetailResponse> getAllBookings();
    BookingResponse approveBooking(Long id);
    BookingResponse rejectBooking(Long id);
}
