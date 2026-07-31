package com.vof.mapper;
import com.vof.dto.response.BookingDetailResponse;
import com.vof.dto.response.BookingSummaryResponse;
import com.vof.dto.response.PaymentDetailResponse;
import com.vof.dto.response.TravellerResponse;
import com.vof.entity.Booking;
import com.vof.entity.PaymentProof;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

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

    public com.vof.dto.response.MyBookingSummaryResponse toMyBookingSummaryResponse(Booking booking) {

        if (booking == null) {
            return null;
        }

        String thumbnailUrl = (booking.getAPackage() != null && !booking.getAPackage().getImages().isEmpty())
                ? booking.getAPackage().getImages().get(0).getImageUrl()
                : null;

        String paymentStatus = booking.getPaymentProof() != null
                ? booking.getPaymentProof().getStatus().name()
                : "NOT_PAID";

        String screenshotUrl = booking.getPaymentProof() != null
                ? booking.getPaymentProof().getScreenshotUrl()
                : null;

        String utrNumber = booking.getPaymentProof() != null
                ? booking.getPaymentProof().getUtr()
                : null;

        LocalDateTime uploadedAt = booking.getPaymentProof() != null
                ? booking.getPaymentProof().getCreatedAt()
                : null;

        return com.vof.dto.response.MyBookingSummaryResponse.builder()
                .bookingId(booking.getBookingId())
                .packageId(booking.getAPackage() != null ? booking.getAPackage().getId() : null)
                .packageName(booking.getAPackage() != null ? booking.getAPackage().getTitle() : null)
                .thumbnailImage(thumbnailUrl)
                .travelDate(booking.getTravelDate())
                .bookingStatus(booking.getStatus())
                .paymentStatus(paymentStatus)

                .utrNumber(utrNumber)
                .screenshotUrl(screenshotUrl)
                .uploadedAt(uploadedAt)

                .adults(booking.getAdults())
                .children(booking.getChildren())
                .totalAmount(booking.getTotalAmount())

                .travellers(
                        booking.getTravellers()
                                .stream()
                                .map(traveller -> TravellerResponse.builder()
                                        .id(traveller.getId())
                                        .fullName(traveller.getFullName())
                                        .age(traveller.getAge())
                                        .gender(traveller.getGender().name())
                                        .phoneNumber(traveller.getPhoneNumber())
                                        .emergencyContact(traveller.getEmergencyContact())
                                        .idProofType(traveller.getIdProofType())
                                        .idProofNumber(traveller.getIdProofNumber())
                                        .medicalCondition(traveller.getMedicalCondition())
                                        .createdAt(traveller.getCreatedAt())
                                        .build())
                                .collect(Collectors.toList())
                )

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
