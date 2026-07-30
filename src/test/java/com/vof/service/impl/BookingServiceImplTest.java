package com.vof.service.impl;

import com.vof.constant.BookingStatus;
import com.vof.dto.request.CreateBookingRequest;
import com.vof.dto.request.UpdateBookingRequest;
import com.vof.dto.response.BookingResponse;
import com.vof.entity.Booking;
import com.vof.entity.Package;
import com.vof.entity.User;
import com.vof.exception.BookingUpdateNotAllowedException;
import com.vof.mapper.BookingMapper;
import com.vof.repository.BookingRepository;
import com.vof.repository.PackageRepository;
import com.vof.repository.UserRepository;
import com.vof.util.BookingIdGenerator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private PackageRepository packageRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BookingIdGenerator bookingIdGenerator;
    @Mock
    private BookingMapper bookingMapper;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private Package testPackage;
    private CreateBookingRequest bookingRequest;
    private User testUser;

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

        testUser = new User();
        testUser.setId(2L);
        testUser.setEmail("test@example.com");

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(testUser.getEmail(), null, java.util.Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createBooking_shouldReturnSuccessfulBookingResponse() {
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
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
        assertEquals(BookingStatus.PENDING.toString(), response.getBookingStatus());
        assertEquals(2500.0, response.getTotalAmount());
        assertTrue(response.getMessage().contains("Booking created successfully"));
    }

    @Test
    void approveBooking_shouldChangeStatusToApproved() {
        Booking pendingBooking = new Booking();
        pendingBooking.setId(1L);
        pendingBooking.setBookingId("BK20260002");
        pendingBooking.setStatus(BookingStatus.PENDING);

        when(bookingRepository.findByBookingId("BK20260002")).thenReturn(Optional.of(pendingBooking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BookingResponse response = bookingService.approveBooking("BK20260002");

        assertNotNull(response);
        assertEquals(BookingStatus.APPROVED.toString(), response.getBookingStatus());
        assertEquals("Booking approved successfully.", response.getMessage());
    }

    @Test
    void getBookingDetails_shouldRejectAnotherUser() {
        User owner = new User();
        owner.setId(3L);
        owner.setEmail("owner@example.com");

        Booking booking = new Booking();
        booking.setBookingId("BK-private");
        booking.setStatus(BookingStatus.PENDING);
        booking.setCreatedBy(owner);

        when(bookingRepository.findByBookingIdAndNotDeleted("BK-private")).thenReturn(Optional.of(booking));

        // The user in the security context is "test@example.com" from setUp()

        assertThrows(AccessDeniedException.class, () -> bookingService.getBookingDetails("BK-private"));
    }

    @Test
    void updateBooking_shouldUpdateDetails_whenStatusIsPending() {
        Booking pendingBooking = new Booking();
        pendingBooking.setBookingId("BK-updatable");
        pendingBooking.setStatus(BookingStatus.PENDING);
        pendingBooking.setCustomerName("Old Name");
        pendingBooking.setCreatedBy(testUser);

        when(bookingRepository.findByBookingId("BK-updatable")).thenReturn(Optional.of(pendingBooking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateBookingRequest updateRequest = new UpdateBookingRequest();
        updateRequest.setCustomerName("New Name");

        BookingResponse response = bookingService.updateBooking("BK-updatable", updateRequest);

        assertEquals("Booking updated successfully.", response.getMessage());
    }

    @Test
    void updateBooking_shouldThrowException_whenStatusIsNotPending() {
        Booking approvedBooking = new Booking();
        approvedBooking.setBookingId("BK-approved");
        approvedBooking.setStatus(BookingStatus.APPROVED);
        approvedBooking.setCreatedBy(testUser);

        when(bookingRepository.findByBookingId("BK-approved")).thenReturn(Optional.of(approvedBooking));

        UpdateBookingRequest updateRequest = new UpdateBookingRequest();
        updateRequest.setCustomerName("New Name");

        assertThrows(BookingUpdateNotAllowedException.class, () -> bookingService.updateBooking("BK-approved", updateRequest));
    }
}
