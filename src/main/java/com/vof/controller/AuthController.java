package com.vof.controller;

import com.vof.dto.request.ChangePasswordRequest;
import com.vof.dto.request.LoginRequest;
import com.vof.dto.request.PasswordOtpRequest;
import com.vof.dto.request.PasswordResetRequest;
import com.vof.dto.request.RegistrationRequest;
import com.vof.dto.request.TokenRefreshRequest;
import com.vof.dto.response.CommonApiResponse;
import com.vof.dto.response.JwtResponse;
import com.vof.dto.response.TokenRefreshResponse;
import com.vof.entity.User;
import com.vof.repository.UserRepository;
import com.vof.service.AuthService;
import com.vof.service.RefreshTokenService;
import com.vof.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private static final String PASSWORD_OTP_SENT_MESSAGE =
            "If that email is registered, a password OTP has been sent to its mobile number.";

    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<CommonApiResponse> register(@Valid @RequestBody RegistrationRequest request) {
        authService.registerMember(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CommonApiResponse.builder().success(true).message("Member registered successfully.").build());
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/password-reset/request-otp")
    public ResponseEntity<CommonApiResponse> requestPasswordResetOtp(@Valid @RequestBody PasswordOtpRequest request) {
        authService.requestPasswordResetOtp(request.getEmail());
        return ResponseEntity.ok(CommonApiResponse.builder().success(true).message(PASSWORD_OTP_SENT_MESSAGE).build());
    }

    @PostMapping("/password-reset/confirm")
    public ResponseEntity<CommonApiResponse> resetPassword(@Valid @RequestBody PasswordResetRequest request) {
        authService.resetForgottenPassword(request);
        return ResponseEntity.ok(CommonApiResponse.builder().success(true).message("Password has been reset. Please log in again.").build());
    }

    @PostMapping("/password-change/request-otp")
    public ResponseEntity<CommonApiResponse> requestPasswordChangeOtp(Authentication authentication) {
        authService.requestPasswordChangeOtp(authentication.getName());
        return ResponseEntity.ok(CommonApiResponse.builder().success(true).message("A password OTP has been sent to your mobile number.").build());
    }

    @PostMapping("/password-change/confirm")
    public ResponseEntity<CommonApiResponse> changePassword(Authentication authentication,
                                                              @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(authentication.getName(), request);
        return ResponseEntity.ok(CommonApiResponse.builder().success(true).message("Password changed. Please log in again.").build());
    }

    @PostMapping("/refreshtoken")
    public ResponseEntity<TokenRefreshResponse> refreshToken(@Valid @RequestBody TokenRefreshRequest request) {
        RefreshTokenService.RefreshTokenRotation rotation = refreshTokenService.rotateRefreshToken(request.getRefreshToken());
        return ResponseEntity.ok(new TokenRefreshResponse(
                jwtUtil.generateTokenFromUsername(rotation.user().getEmail()), rotation.token()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("Authenticated member no longer exists."));
        refreshTokenService.deleteByUserId(user.getId());
        return ResponseEntity.noContent().build();
    }
}
