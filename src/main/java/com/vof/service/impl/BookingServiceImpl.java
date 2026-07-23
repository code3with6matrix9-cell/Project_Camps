package com.vof.service.impl;

import com.vof.constant.BookingStatus;
import com.vof.dto.request.CreateBookingRequest;
import com.vof.dto.response.BookingDetailResponse;
import com.vof.dto.response.BookingResponse;
import com.vof.entity.Booking;
import com.vof.entity.Package;
import com.vof.entity.User;
import com.vof.exception.ResourceNotFoundException;
import com.vof.mapper.BookingMapper;
import com.vof.repository.BookingRepository;
import com.vof.repository.PackageRepository;
import com.vof.repository.UserRepository;
import com.vof.service.BookingService;
import com.vof.util.BookingIdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

        Booking savedBooking = bookingRepository.save(booking);

        int adults = request.getAdults();
        int children = request.getChildren() != null ? request.getChildren() : 0;
        // Adults pay full price, children pay 50% of the package price
        double totalAmount = (pkg.getPrice() != null ? pkg.getPrice() : 0.0) * adults
                           + (pkg.getPrice() != null ? pkg.getPrice() * 0.5 : 0.0) * children;

        return BookingResponse.builder()
                .bookingId(savedBooking.getBookingId())
                .status(savedBooking.getStatus().toString())
                .totalAmount(totalAmount)
                .message("Booking created successfully. Please complete the payment.")
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public BookingResponse getBookingStatus(String bookingId) {
        Booking booking = bookingRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));
        requireBookingAccess(booking);
        return BookingResponse.builder()
                .bookingId(booking.getBookingId())
                .status(booking.getStatus().toString())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingDetailResponse> getAllBookings() {
        return bookingRepository.findAll().stream()
                .map(bookingMapper::toBookingDetailResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public BookingResponse approveBooking(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + id));
        booking.setStatus(BookingStatus.APPROVED);
        bookingRepository.save(booking);
        return BookingResponse.builder()
                .bookingId(booking.getBookingId())
                .status(booking.getStatus().toString())
                .message("Booking approved successfully.")
                .build();
    }

    @Override
    @Transactional
    public BookingResponse rejectBooking(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + id));
        booking.setStatus(BookingStatus.REJECTED);
        bookingRepository.save(booking);
        return BookingResponse.builder()
                .bookingId(booking.getBookingId())
                .status(booking.getStatus().toString())
                .message("Booking rejected successfully.")
                .build();
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
        boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin && !booking.getCreatedBy().getEmail().equals(authentication.getName())) {
            throw new org.springframework.security.access.AccessDeniedException("Booking does not belong to the current user.");
        }
    }
}
