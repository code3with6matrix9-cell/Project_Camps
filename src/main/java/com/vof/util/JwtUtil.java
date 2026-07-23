package com.vof.util;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
@Component
public class JwtUtil {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);
    @Value("${app.jwt.secret}") private String jwtSecret;
    @Value("${app.jwt.expiration-ms}") private long jwtExpirationMs;
    @Value("${app.jwt.issuer}") private String issuer;
    @Value("${app.jwt.audience}") private String audience;
    private SecretKey signingKey;

    @PostConstruct
    void initialize() {
        byte[] keyBytes;
        try {
            keyBytes = Base64.getDecoder().decode(jwtSecret.getBytes(StandardCharsets.US_ASCII));
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException("app.jwt.secret must be Base64 encoded", exception);
        }
        if (keyBytes.length < 64) {
            throw new IllegalStateException("app.jwt.secret must decode to at least 64 bytes for HS512");
        }
        signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    private SecretKey getSigningKey() { return signingKey; }
    public String generateJwtToken(Authentication authentication) { return generateTokenFromUsername(((UserDetails) authentication.getPrincipal()).getUsername()); }
    public String generateTokenFromUsername(String username) {
        Date now = new Date();
        return Jwts.builder().setSubject(username).setIssuer(issuer).setAudience(audience).setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + jwtExpirationMs)).claim("token_type", "access")
                .signWith(getSigningKey(), SignatureAlgorithm.HS512).compact();
    }
    public String getUserNameFromJwtToken(String token) { return Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token).getBody().getSubject(); }
    public boolean validateJwtToken(String authToken) {
        try {
            Claims claims = Jwts.parserBuilder().requireIssuer(issuer).requireAudience(audience)
                    .setSigningKey(getSigningKey()).build().parseClaimsJws(authToken).getBody();
            return "access".equals(claims.get("token_type", String.class));
        } catch (MalformedJwtException | UnsupportedJwtException | IllegalArgumentException e) {
            logger.warn("Rejected malformed JWT");
        } catch (ExpiredJwtException e) {
            logger.debug("Rejected expired JWT");
        } catch (JwtException e) {
            logger.warn("Rejected JWT with invalid claims");
        }
        return false;
    }
}
