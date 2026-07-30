package com.vof.mapper;
import com.vof.dto.response.BookingDetailResponse;
import com.vof.dto.response.BookingSummaryResponse;
import com.vof.dto.response.PaymentDetailResponse;
import com.vof.entity.Booking;
import com.vof.entity.PaymentProof;
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

    public com.vof.dto.response.MyBookingSummaryResponse toMyBookingSummaryResponse(com.vof.entity.Booking booking) {
        if (booking == null) return null;
        String thumbnailUrl = (booking.getAPackage() != null && !booking.getAPackage().getImages().isEmpty())
                ? booking.getAPackage().getImages().get(0).getImageUrl()
                : null;

        String paymentStatus = (booking.getPaymentProof() != null)
                ? booking.getPaymentProof().getStatus().toString()
                : "NOT_PAID";

        return com.vof.dto.response.MyBookingSummaryResponse.builder()
                .bookingId(booking.getBookingId())
                .packageId(booking.getAPackage() != null ? booking.getAPackage().getId() : null)
                .packageName(booking.getAPackage() != null ? booking.getAPackage().getTitle() : null)
                .thumbnailImage(thumbnailUrl)
                .travelDate(booking.getTravelDate())
                .bookingStatus(booking.getStatus())
                .paymentStatus(paymentStatus)
                .adults(booking.getAdults())
                .children(booking.getChildren())
                .totalAmount(booking.getTotalAmount())
                .createdAt(booking.getCreatedAt())
                .build();
    }

    public BookingSummaryResponse toBookingSummaryResponse(Booking booking) {
        if (booking == null) return null;

        String thumbnailUrl = (booking.getAPackage() != null && !booking.getAPackage().getImages().isEmpty())
                ? booking.getAPackage().getImages().get(0).getImageUrl()
                : null;

        String paymentStatus = (booking.getPaymentProof() != null)
                ? booking.getPaymentProof().getStatus().toString()
                : "NOT_PAID";

        return BookingSummaryResponse.builder()
                .bookingId(booking.getBookingId())
                .packageId(booking.getAPackage() != null ? booking.getAPackage().getId() : null)
                .packageName(booking.getAPackage() != null ? booking.getAPackage().getTitle() : null)
                .thumbnailImage(thumbnailUrl)
                .travelDate(booking.getTravelDate())
                .bookingStatus(booking.getStatus())
                .paymentStatus(paymentStatus)
                .adults(booking.getAdults())
                .children(booking.getChildren())
                .totalAmount(booking.getTotalAmount())
                .createdAt(booking.getCreatedAt())
                .build();
    }

    public PaymentDetailResponse toPaymentDetailResponse(PaymentProof paymentProof) {

        if (paymentProof == null) {
            return null;
        }

        return PaymentDetailResponse.builder()
                .bookingId(paymentProof.getBooking().getBookingId())
                .paymentStatus(paymentProof.getStatus().name())
                .amount(paymentProof.getAmount())
                .utrNumber(paymentProof.getUtr())
                .screenshotUrl(paymentProof.getScreenshotUrl())
                .uploadedAt(paymentProof.getCreatedAt())
                .build();
    }
}
