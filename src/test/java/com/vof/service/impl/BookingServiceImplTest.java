package com.vof.service.impl;

import com.vof.constant.BookingStatus;
import com.vof.dto.request.CreateBookingRequest;
import com.vof.dto.response.BookingResponse;
import com.vof.entity.Booking;
import com.vof.entity.Package;
import com.vof.dto.response.BookingDetailResponse;
import com.vof.mapper.BookingMapper;
import com.vof.repository.BookingRepository;
import com.vof.repository.PackageRepository;
import com.vof.repository.UserRepository;
import com.vof.entity.User;
import com.vof.util.BookingIdGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock private BookingRepository bookingRepository;
    @Mock private PackageRepository packageRepository;
    @Mock private UserRepository userRepository;
    @Mock private BookingIdGenerator bookingIdGenerator;
    @Mock private BookingMapper bookingMapper;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private Package testPackage;
    private CreateBookingRequest bookingRequest;

    @BeforeEach
    void setUp() {
        testPackage = new Package();
        testPackage.setId(1L);
        testPackage.setTitle("Test Package");
        testPackage.setPrice(1000.0);

        bookingRequest = new CreateBookingRequest();
        bookingRequest.setPackageId(1L);
        bookingRequest.setCustomerName("Test User");
        bookingRequest.setEmail("test@user.com");
        bookingRequest.setPhone("1234567890");
        bookingRequest.setAdults(2);
        bookingRequest.setChildren(1);
        bookingRequest.setTravelDate(LocalDate.now().plusDays(10));
    }

    @AfterEach
    void clearSecurityContext() {
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
    }

    @Test
    void createBooking_shouldReturnSuccessfulBookingResponse() {
        User user = new User();
        user.setId(2L);
        user.setEmail("test@example.com");
        org.springframework.security.authentication.UsernamePasswordAuthenticationToken authentication =
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken("test@example.com", null, java.util.List.of());
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(authentication);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(packageRepository.findById(1L)).thenReturn(Optional.of(testPackage));
        when(bookingIdGenerator.generateBookingId()).thenReturn("BK20260001");
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking booking = invocation.getArgument(0);
            booking.setId(1L);
            return booking;
        });

        BookingResponse response = bookingService.createBooking(bookingRequest);

        assertNotNull(response);
        assertEquals("BK20260001", response.getBookingId());
        assertEquals(BookingStatus.PENDING.toString(), response.getStatus());
        // 2 adults * 1000 + 1 child * 500 = 2500
        assertEquals(2500.0, response.getTotalAmount());
        assertTrue(response.getMessage().contains("Booking created successfully"));
    }

    @Test
    void approveBooking_shouldChangeStatusToApproved() {
        Booking pendingBooking = new Booking();
        pendingBooking.setId(1L);
        pendingBooking.setBookingId("BK20260002");
        pendingBooking.setStatus(BookingStatus.PENDING);

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(pendingBooking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BookingResponse response = bookingService.approveBooking(String.valueOf(1L));

        assertNotNull(response);
        assertEquals(BookingStatus.APPROVED.toString(), response.getStatus());
        assertEquals("Booking approved successfully.", response.getMessage());
    }

    @Test
    void getBookingStatus_shouldRejectAnotherUser() {
        User owner = new User();
        owner.setId(2L);
        owner.setEmail("owner@example.com");
        Booking booking = new Booking();
        booking.setBookingId("BK-private");
        booking.setStatus(BookingStatus.PENDING);
        booking.setCreatedBy(owner);
        when(bookingRepository.findByBookingId("BK-private")).thenReturn(Optional.of(booking));
        org.springframework.security.authentication.UsernamePasswordAuthenticationToken authentication =
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken("other@example.com", null, java.util.List.of());
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(authentication);

        assertThrows(org.springframework.security.access.AccessDeniedException.class,
                () -> bookingService.getBookingStatus("BK-private"));
    }
}
