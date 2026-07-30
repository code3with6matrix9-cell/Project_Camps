package com.vof.service.impl;

import com.vof.constant.BookingStatus;
import com.vof.dto.request.CreateBookingRequest;
import com.vof.dto.request.UpdateBookingRequest;
import com.vof.dto.response.BookingDetailResponse;
import com.vof.dto.response.BookingResponse;
import com.vof.dto.response.PaymentDetailResponse;
import com.vof.dto.response.TravellerResponse;
import com.vof.entity.Booking;
import com.vof.entity.Package;
import com.vof.entity.PaymentProof;
import com.vof.entity.Traveller;
import com.vof.entity.User;
import com.vof.exception.BookingUpdateNotAllowedException;
import com.vof.exception.PaymentNotFoundException;
import com.vof.exception.ResourceNotFoundException;
import com.vof.mapper.BookingMapper;
import com.vof.repository.BookingRepository;
import com.vof.repository.PackageRepository;
import com.vof.repository.UserRepository;
import com.vof.service.BookingService;
import com.vof.util.BookingIdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final PackageRepository packageRepository;
    private final UserRepository userRepository;
    private final BookingIdGenerator bookingIdGenerator;
    private final BookingMapper bookingMapper;

    @Override
    @Transactional
    public BookingResponse createBooking(CreateBookingRequest request) {
        Package pkg = packageRepository.findById(request.getPackageId())
                .orElseThrow(() -> new ResourceNotFoundException("Package not found with id: " + request.getPackageId()));

        int expectedTravellerCount = request.getAdults() + request.getChildren();
        if (request.getTravellers() == null || request.getTravellers().size() != expectedTravellerCount) {
            throw new IllegalArgumentException("Number of travellers must match adults + children count");
        }

        Booking booking = new Booking();
        booking.setCreatedBy(currentUser());
        booking.setCustomerName(request.getCustomerName());
        booking.setEmail(request.getEmail());
        booking.setPhone(request.getPhone());
        booking.setAPackage(pkg);
        booking.setTravelDate(request.getTravelDate());
        booking.setAdults(request.getAdults());
        booking.setChildren(request.getChildren());
        booking.setSpecialRequest(request.getSpecialRequest());
        booking.setStatus(BookingStatus.PENDING);
        booking.setBookingId(bookingIdGenerator.generateBookingId());
        booking.setPickupPoint(request.getPickupPoint());

        double totalAmount = (pkg.getPrice() != null ? pkg.getPrice() : 0.0) * request.getAdults()
                           + (pkg.getPrice() != null ? pkg.getPrice() * 0.5 : 0.0) * request.getChildren();
        booking.setTotalAmount(totalAmount);

        List<Traveller> travellers = request.getTravellers().stream()
                .map(t -> {
                    Traveller traveller = new Traveller();
                    traveller.setFullName(t.getFullName());
                    traveller.setAge(t.getAge());
                    traveller.setGender(Traveller.Gender.valueOf(t.getGender().toUpperCase()));
                    traveller.setPhoneNumber(t.getPhoneNumber());
                    traveller.setEmergencyContact(t.getEmergencyContact());
                    traveller.setIdProofType(t.getIdProofType());
                    traveller.setIdProofNumber(t.getIdProofNumber());
                    traveller.setMedicalCondition(t.getMedicalCondition());
                    return traveller;
                })
                .collect(Collectors.toList());
        booking.setTravellers(travellers);
        travellers.forEach(t -> t.setBooking(booking));

        Booking savedBooking = bookingRepository.save(booking);

        return BookingResponse.builder()
                .bookingId(savedBooking.getBookingId())
                .message("Booking created successfully. Please complete the payment.")
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public BookingResponse getBookingDetails(String bookingId) {
        Booking booking = bookingRepository.findByBookingIdAndNotDeleted(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));
        requireBookingAccess(booking);
        return buildBookingResponse(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingDetailResponse> getAllBookings() {
        return bookingRepository.findAllNotDeleted().stream()
                .map(bookingMapper::toBookingDetailResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public BookingResponse approveBooking(String bookingId) {
        Booking booking = bookingRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with bookingId: " + bookingId));
        booking.setStatus(BookingStatus.APPROVED);
        bookingRepository.save(booking);
        return BookingResponse.builder()
                .bookingId(booking.getBookingId())
                .bookingStatus(booking.getStatus().toString())
                .message("Booking approved successfully.")
                .build();
    }

    @Override
    @Transactional
    public BookingResponse rejectBooking(String bookingId) {
        Booking booking = bookingRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with bookingId: " + bookingId));
        booking.setStatus(BookingStatus.REJECTED);
        bookingRepository.save(booking);
        return BookingResponse.builder()
                .bookingId(booking.getBookingId())
                .bookingStatus(booking.getStatus().toString())
                .message("Booking rejected successfully.")
                .build();
    }

    @Override
    @Transactional
    public BookingResponse updateBooking(String bookingId, UpdateBookingRequest request) {
        Booking booking = bookingRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with bookingId: " + bookingId));
        requireBookingAccess(booking);
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new BookingUpdateNotAllowedException("Booking can only be updated when its status is PENDING. Current status: " + booking.getStatus());
        }
        if (request.getCustomerName() != null) booking.setCustomerName(request.getCustomerName());
        if (request.getPhone() != null) booking.setPhone(request.getPhone());
        if (request.getSpecialRequest() != null) booking.setSpecialRequest(request.getSpecialRequest());
        if (request.getPickupPoint() != null) booking.setPickupPoint(request.getPickupPoint());
        Booking updatedBooking = bookingRepository.save(booking);
        return BookingResponse.builder()
                .bookingId(updatedBooking.getBookingId())
                .bookingStatus(updatedBooking.getStatus().toString())
                .message("Booking updated successfully.")
                .build();
    }

    @Override
    @Transactional
    public void deleteBooking(String bookingId) {
        Booking booking = bookingRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with bookingId: " + bookingId));
        booking.setStatus(BookingStatus.DELETED);
        bookingRepository.save(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentDetailResponse getPaymentDetails(String bookingId) {
        Booking booking = bookingRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with bookingId: " + bookingId));
        PaymentProof paymentProof = booking.getPaymentProof();
        if (paymentProof == null) {
            throw new PaymentNotFoundException("Payment proof not found for bookingId: " + bookingId);
        }
        return PaymentDetailResponse.builder()
                .bookingId(booking.getBookingId())
                .paymentStatus(paymentProof.getStatus().toString())
                .amount(paymentProof.getAmount() != null ? paymentProof.getAmount().doubleValue() : 0.0)
                .utrNumber(paymentProof.getUtr())
                .screenshotUrl(paymentProof.getScreenshotUrl())
                .uploadedAt(paymentProof.getCreatedAt())
                .build();
    }

    private BookingResponse buildBookingResponse(Booking booking) {
        String paymentStatus = "NOT_PAID";
        String utrNumber = null;
        String paymentProofUrl = null;
        if (booking.getPaymentProof() != null) {
            PaymentProof pp = booking.getPaymentProof();
            paymentStatus = pp.getStatus().toString();
            utrNumber = pp.getUtr();
            paymentProofUrl = pp.getScreenshotUrl();
        }
        List<TravellerResponse> travellerResponses = booking.getTravellers().stream()
                .map(t -> TravellerResponse.builder()
                        .id(t.getId()).fullName(t.getFullName()).age(t.getAge())
                        .gender(t.getGender() != null ? t.getGender().toString() : null)
                        .phoneNumber(t.getPhoneNumber()).emergencyContact(t.getEmergencyContact())
                        .idProofType(t.getIdProofType()).idProofNumber(t.getIdProofNumber())
                        .medicalCondition(t.getMedicalCondition()).createdAt(t.getCreatedAt()).build())
                .collect(Collectors.toList());
        return BookingResponse.builder()
                .bookingId(booking.getBookingId()).bookingStatus(booking.getStatus().toString())
                .paymentStatus(paymentStatus)
                .packageName(booking.getAPackage() != null ? booking.getAPackage().getTitle() : null)
                .packageId(booking.getAPackage() != null ? booking.getAPackage().getId() : null)
                .travelDate(booking.getTravelDate()).bookingDate(booking.getCreatedAt())
                .totalAmount(booking.getTotalAmount()).adults(booking.getAdults()).children(booking.getChildren())
                .specialRequest(booking.getSpecialRequest()).pickupPoint(booking.getPickupPoint())
                .customerName(booking.getCustomerName()).email(booking.getEmail()).phone(booking.getPhone())
                .utrNumber(utrNumber).paymentProofUrl(paymentProofUrl).travellers(travellerResponses).build();
    }

    private User currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new org.springframework.security.access.AccessDeniedException("Authentication is required.");
        }
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new org.springframework.security.access.AccessDeniedException("Authenticated user no longer exists."));
    }

    private void requireBookingAccess(Booking booking) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new org.springframework.security.access.AccessDeniedException("Authentication is required.");
        }
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin && !booking.getCreatedBy().getEmail().equals(authentication.getName())) {
            throw new org.springframework.security.access.AccessDeniedException("Booking does not belong to the current user.");
        }
    }
}
