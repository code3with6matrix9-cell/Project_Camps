package com.vof.service;
import com.vof.entity.RefreshToken;
import com.vof.entity.User;
import com.vof.exception.TokenRefreshException;
import com.vof.repository.RefreshTokenRepository;
import com.vof.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.Optional;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    @Value("${app.jwt.refresh-expiration-ms}") private Long refreshTokenDurationMs;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    public Optional<RefreshToken> findByToken(String token) { return refreshTokenRepository.findByToken(hash(token)); }

    @Transactional
    public String createRefreshToken(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User does not exist."));

        // Find existing token or create a new one
        RefreshToken refreshToken = refreshTokenRepository.findByUser(user)
                .orElse(new RefreshToken());

        // Update the token with new values
        refreshToken.setUser(user);
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
        String rawToken = generateToken();
        refreshToken.setToken(hash(rawToken));

        // Save the updated or new token
        refreshTokenRepository.save(refreshToken);

        return rawToken;
    }
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(token);
            throw new TokenRefreshException("Refresh token has expired.");
        }
        return token;
    }
    @Transactional
    public RefreshTokenRotation rotateRefreshToken(String rawToken) {
        RefreshToken token = findByToken(rawToken)
                .orElseThrow(() -> new TokenRefreshException("Refresh token is invalid."));
        RefreshToken verifiedToken = verifyExpiration(token);
        String newToken = createRefreshToken(verifiedToken.getUser().getId());
        return new RefreshTokenRotation(verifiedToken.getUser(), newToken);
    }

    @Transactional
    public int deleteByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User does not exist."));
        return refreshTokenRepository.deleteByUser(user);
    }

    private String generateToken() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String value) {
        try {
            return Base64.getEncoder().encodeToString(MessageDigest.getInstance("SHA-256").digest(value.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    public record RefreshTokenRotation(com.vof.entity.User user, String token) { }
}
