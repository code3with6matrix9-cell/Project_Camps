package com.vof.service;

import com.vof.constant.RoleConstant;
import com.vof.dto.request.PasswordResetRequest;
import com.vof.dto.request.RegistrationRequest;
import com.vof.entity.PasswordOtp;
import com.vof.entity.Role;
import com.vof.entity.User;
import com.vof.repository.PasswordOtpRepository;
import com.vof.repository.RoleRepository;
import com.vof.repository.UserRepository;
import com.vof.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock private AuthenticationManager authenticationManager;
    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private PasswordOtpRepository passwordOtpRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private RefreshTokenService refreshTokenService;
    @Mock private SmsSender smsSender;
    @Mock private JwtUtil jwtUtil;
    @InjectMocks private AuthService authService;
    @Captor private ArgumentCaptor<User> userCaptor;
    @Captor private ArgumentCaptor<PasswordOtp> passwordOtpCaptor;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "otpExpirationMinutes", 10L);
        ReflectionTestUtils.setField(authService, "maxOtpFailedAttempts", 5);
        ReflectionTestUtils.setField(authService, "otpRequestCooldownSeconds", 60L);
    }

    @Test
    void registerMember_usesEmailAsTheOnlyLoginIdentityAndStoresMobileNumber() {
        RegistrationRequest request = new RegistrationRequest();
        request.setEmail(" Member@Example.com ");
        request.setMobileNumber("+919876543210");
        request.setPassword("a-secure-password");
        Role memberRole = new Role();
        memberRole.setName(RoleConstant.ROLE_USER);
        when(userRepository.existsByEmail("member@example.com")).thenReturn(false);
        when(userRepository.existsByMobileNumber("+919876543210")).thenReturn(false);
        when(roleRepository.findByName(RoleConstant.ROLE_USER)).thenReturn(Optional.of(memberRole));
        when(passwordEncoder.encode("a-secure-password")).thenReturn("encoded-password");

        authService.registerMember(request);

        verify(userRepository).save(userCaptor.capture());
        User member = userCaptor.getValue();
        assertEquals("member@example.com", member.getEmail());
        assertEquals("member@example.com", member.getUsername());
        assertEquals("+919876543210", member.getMobileNumber());
        assertEquals("encoded-password", member.getPassword());
        assertTrue(member.getRoles().contains(memberRole));
    }

    @Test
    void requestPasswordResetOtp_storesOnlyAHashAndSendsThePlaintextCodeBySms() {
        User member = new User("member@example.com", "+919876543210", "old-password");
        when(userRepository.findByEmail("member@example.com")).thenReturn(Optional.of(member));
        when(passwordOtpRepository.findByUser(member)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(any())).thenReturn("bcrypt-otp");

        authService.requestPasswordResetOtp("MEMBER@example.com");

        verify(passwordOtpRepository).save(passwordOtpCaptor.capture());
        PasswordOtp storedOtp = passwordOtpCaptor.getValue();
        assertEquals(member, storedOtp.getUser());
        assertEquals("bcrypt-otp", storedOtp.getOtpHash());
        assertTrue(storedOtp.getExpiresAt().isAfter(Instant.now()));
        verify(smsSender).sendPasswordOtp(eq("+919876543210"), argThat(code -> code.matches("\\d{6}")));
    }

    @Test
    void resetForgottenPassword_consumesOtpAndInvalidatesAllRefreshTokens() {
        User member = new User("member@example.com", "+919876543210", "old-password");
        member.setId(17L);
        PasswordOtp storedOtp = new PasswordOtp();
        storedOtp.setUser(member);
        storedOtp.setOtpHash("bcrypt-otp");
        storedOtp.setExpiresAt(Instant.now().plusSeconds(300));
        storedOtp.setIssuedAt(Instant.now());
        PasswordResetRequest request = new PasswordResetRequest();
        request.setEmail("MEMBER@example.com");
        request.setOtp("123456");
        request.setNewPassword("new-secure-password");
        when(userRepository.findByEmail("member@example.com")).thenReturn(Optional.of(member));
        when(passwordOtpRepository.findByUser(member)).thenReturn(Optional.of(storedOtp));
        when(passwordEncoder.matches("123456", "bcrypt-otp")).thenReturn(true);
        when(passwordEncoder.encode("new-secure-password")).thenReturn("new-password-hash");

        authService.resetForgottenPassword(request);

        assertEquals("new-password-hash", member.getPassword());
        verify(passwordOtpRepository).delete(storedOtp);
        verify(refreshTokenService).deleteByUserId(17L);
    }
}
