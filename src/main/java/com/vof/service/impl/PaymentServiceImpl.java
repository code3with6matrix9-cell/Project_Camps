package com.vof.service.impl;
import com.vof.constant.PaymentStatus;
import com.vof.dto.request.PaymentProofRequest;
import com.vof.dto.response.PaymentResponse;
import com.vof.entity.Booking;
import com.vof.entity.PaymentProof;
import com.vof.entity.User;
import com.vof.exception.ResourceNotFoundException;
import com.vof.repository.BookingRepository;
import com.vof.repository.PaymentProofRepository;
import com.vof.repository.UserRepository;
import com.vof.service.CloudinaryService;
import com.vof.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import java.io.IOException;
@Service @RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    private final PaymentProofRepository paymentProofRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;
    @Override @Transactional
    public PaymentResponse uploadPaymentProof(PaymentProofRequest request) throws IOException {
        Booking booking = bookingRepository.findByBookingId(request.getBookingId()).orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + request.getBookingId()));
        requireBookingAccess(booking);
        if (paymentProofRepository.existsByBooking(booking)) throw new IllegalArgumentException("A payment proof has already been submitted for this booking.");
        if (paymentProofRepository.existsByUtr(request.getUtrNumber())) throw new IllegalArgumentException("This UTR number has already been used.");
        double expectedAmount = booking.getAPackage().getPrice() * booking.getAdults()
                + (booking.getAPackage().getPrice() * 0.5 * booking.getChildren());
        if (Math.abs(request.getAmount() - expectedAmount) > 0.009d) {
            throw new IllegalArgumentException("Submitted amount does not match the booking total.");
        }
        if (request.getScreenshot() == null || request.getScreenshot().isEmpty()) throw new IllegalArgumentException("Screenshot file is mandatory.");
        if (request.getScreenshot().getSize() > 10 * 1024 * 1024) throw new IllegalArgumentException("Screenshot must not exceed 10 MB.");
        String contentType = request.getScreenshot().getContentType();
        if (contentType == null || !(contentType.equals("image/jpeg") || contentType.equals("image/png") || contentType.equals("image/webp"))) {
            throw new IllegalArgumentException("Screenshot must be a JPEG, PNG, or WebP image.");
        }
        String screenshotUrl = cloudinaryService.uploadFile(request.getScreenshot());
        PaymentProof paymentProof = new PaymentProof();
        paymentProof.setBooking(booking);
        paymentProof.setUtr(request.getUtrNumber());
        paymentProof.setAmount(request.getAmount());
        paymentProof.setScreenshotUrl(screenshotUrl);
        paymentProof.setStatus(PaymentStatus.UNDER_VERIFICATION);
        PaymentProof savedProof = paymentProofRepository.save(paymentProof);
        booking.setPaymentProof(savedProof);
        bookingRepository.save(booking);
        return PaymentResponse.builder().paymentId(String.valueOf(savedProof.getId())).status(savedProof.getStatus().toString()).screenshotUrl(savedProof.getScreenshotUrl()).build();
    }

    private void requireBookingAccess(Booking booking) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
        if (isAdmin) return;
        User currentUser = userRepository.findByEmail(authentication == null ? "" : authentication.getName())
                .orElseThrow(() -> new org.springframework.security.access.AccessDeniedException("Authentication is required."));
        if (!booking.getCreatedBy().getId().equals(currentUser.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("Booking does not belong to the current user.");
        }
    }
}
