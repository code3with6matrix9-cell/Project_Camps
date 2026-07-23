package com.vof.config;
import com.vof.constant.RoleConstant;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.beans.factory.annotation.Value;
import java.util.Arrays;
@Configuration @EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtRequestFilter jwtRequestFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final RestAccessDeniedHandler restAccessDeniedHandler;
    @Bean public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
    @Bean public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception { return authenticationConfiguration.getAuthenticationManager(); }
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // CSRF disabled because this is a stateless REST API using JWT tokens.
        // The application does not serve HTML pages, so there is no risk of CSRF
        // from session-based attacks. If browser-based form rendering is ever added,
        // CSRF must be re-enabled.
        http.csrf(csrf -> csrf.disable())
            .cors(cors -> { })
            .exceptionHandling(exception -> exception.authenticationEntryPoint(jwtAuthenticationEntryPoint).accessDeniedHandler(restAccessDeniedHandler))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers(HttpMethod.POST, "/api/auth/login", "/api/auth/register", "/api/auth/refreshtoken",
                        "/api/auth/password-reset/request-otp", "/api/auth/password-reset/confirm").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/password-change/**", "/api/auth/logout").authenticated()
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                .requestMatchers("/api/admin/**").hasRole(RoleConstant.ADMIN)
                .requestMatchers("/api/bookings/**", "/api/payment-proof/**").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/contact").permitAll()
                .anyRequest().permitAll());
        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(@Value("${app.cors.allowed-origins:}") String allowedOrigins) {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.stream(allowedOrigins.split(",")).map(String::trim).filter(value -> !value.isEmpty()).toList());
        configuration.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(java.util.List.of("Authorization", "Content-Type"));
        configuration.setExposedHeaders(java.util.List.of("Location"));
        configuration.setAllowCredentials(false);
        configuration.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
