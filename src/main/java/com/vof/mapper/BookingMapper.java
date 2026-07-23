package com.vof.mapper;
import com.vof.dto.response.BookingDetailResponse;
import com.vof.entity.Booking;
import org.springframework.stereotype.Component;
@Component
public class BookingMapper {
    public BookingDetailResponse toBookingDetailResponse(Booking booking) {
        if (booking == null) return null;
        return BookingDetailResponse.builder()
                .id(booking.getId()).bookingId(booking.getBookingId())
                .customerName(booking.getCustomerName()).email(booking.getEmail()).phone(booking.getPhone())
                .adults(booking.getAdults()).children(booking.getChildren())
                .travelDate(booking.getTravelDate()).specialRequest(booking.getSpecialRequest())
                .status(booking.getStatus().toString()).createdAt(booking.getCreatedAt())
                .packageName(booking.getAPackage() != null ? booking.getAPackage().getTitle() : null)
                .build();
    }
}
