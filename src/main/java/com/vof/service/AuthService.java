package com.vof.service;

import com.vof.constant.RoleConstant;
import com.vof.dto.request.ChangePasswordRequest;
import com.vof.dto.request.LoginRequest;
import com.vof.dto.request.PasswordResetRequest;
import com.vof.dto.request.RegistrationRequest;
import com.vof.dto.response.JwtResponse;
import com.vof.entity.PasswordOtp;
import com.vof.entity.Role;
import com.vof.entity.User;
import com.vof.repository.PasswordOtpRepository;
import com.vof.repository.RoleRepository;
import com.vof.repository.UserRepository;
import com.vof.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {
    private static final SecureRandom OTP_RANDOM = new SecureRandom();
    private static final int OTP_LENGTH = 6;

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordOtpRepository passwordOtpRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;
    private final SmsSender smsSender;
    private final JwtUtil jwtUtil;

    @Value("${app.password-otp.expiration-minutes:10}")
    private long otpExpirationMinutes;
    @Value("${app.password-otp.max-failed-attempts:5}")
    private int maxOtpFailedAttempts;
    @Value("${app.password-otp.request-cooldown-seconds:60}")
    private long otpRequestCooldownSeconds;

    @Transactional
    public void registerMember(RegistrationRequest request) {
        String email = normalizeEmail(request.getEmail());
        String mobileNumber = request.getMobileNumber().trim();
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email is already registered.");
        }
        if (userRepository.existsByMobileNumber(mobileNumber)) {
            throw new IllegalArgumentException("Mobile number is already registered.");
        }
        Role memberRole = roleRepository.findByName(RoleConstant.ROLE_USER)
                .orElseThrow(() -> new IllegalStateException("Default member role is not configured."));
        User member = new User(email, mobileNumber, passwordEncoder.encode(request.getPassword()));
        member.setRoles(Set.of(memberRole));
        userRepository.save(member);
    }

    public JwtResponse login(LoginRequest request) {
        String email = normalizeEmail(request.getEmail());
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.getPassword()));
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Authenticated member no longer exists."));
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        return new JwtResponse(jwtUtil.generateJwtToken(authentication), refreshTokenService.createRefreshToken(user.getId()),
                user.getId(), user.getEmail(), roles);
    }

    /** Always succeeds from the caller's perspective to avoid account enumeration. */
    @Transactional
    public void requestPasswordResetOtp(String requestedEmail) {
        userRepository.findByEmail(normalizeEmail(requestedEmail)).ifPresent(this::createAndSendOtp);
    }

    @Transactional
    public void resetForgottenPassword(PasswordResetRequest request) {
        User user = getUserByEmail(request.getEmail());
        verifyOtp(user, request.getOtp());
        updatePasswordAndInvalidateSessions(user, request.getNewPassword());
    }

    @Transactional
    public void requestPasswordChangeOtp(String authenticatedEmail) {
        createAndSendOtp(getUserByEmail(authenticatedEmail));
    }

    @Transactional
    public void changePassword(String authenticatedEmail, ChangePasswordRequest request) {
        User user = getUserByEmail(authenticatedEmail);
        verifyOtp(user, request.getOtp());
        updatePasswordAndInvalidateSessions(user, request.getNewPassword());
    }

    private void createAndSendOtp(User user) {
        Instant now = Instant.now();
        PasswordOtp existingOtp = passwordOtpRepository.findByUser(user).orElse(null);
        if (existingOtp != null && existingOtp.getIssuedAt().plusSeconds(otpRequestCooldownSeconds).isAfter(now)) {
            return;
        }
        String otp = generateOtp();
        PasswordOtp passwordOtp = existingOtp == null ? new PasswordOtp() : existingOtp;
        passwordOtp.setUser(user);
        passwordOtp.setOtpHash(passwordEncoder.encode(otp));
        passwordOtp.setExpiresAt(now.plusSeconds(otpExpirationMinutes * 60));
        passwordOtp.setIssuedAt(now);
        passwordOtp.setFailedAttempts(0);
        passwordOtpRepository.save(passwordOtp);
        smsSender.sendPasswordOtp(user.getMobileNumber(), otp);
    }

    private void verifyOtp(User user, String otp) {
        PasswordOtp passwordOtp = passwordOtpRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("OTP is invalid or has expired."));
        if (passwordOtp.getExpiresAt().isBefore(Instant.now()) || passwordOtp.getFailedAttempts() >= maxOtpFailedAttempts) {
            passwordOtpRepository.delete(passwordOtp);
            throw new IllegalArgumentException("OTP is invalid or has expired.");
        }
        if (!passwordEncoder.matches(otp, passwordOtp.getOtpHash())) {
            passwordOtp.setFailedAttempts(passwordOtp.getFailedAttempts() + 1);
            passwordOtpRepository.save(passwordOtp);
            throw new IllegalArgumentException("OTP is invalid or has expired.");
        }
        passwordOtpRepository.delete(passwordOtp);
    }

    private void updatePasswordAndInvalidateSessions(User user, String newPassword) {
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        refreshTokenService.deleteByUserId(user.getId());
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(normalizeEmail(email))
                .orElseThrow(() -> new IllegalArgumentException("OTP is invalid or has expired."));
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String generateOtp() {
        return String.format(Locale.ROOT, "%0" + OTP_LENGTH + "d", OTP_RANDOM.nextInt(1_000_000));
    }
}
