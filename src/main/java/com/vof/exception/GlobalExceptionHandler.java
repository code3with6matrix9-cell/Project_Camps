package com.vof.exception;
import com.vof.dto.response.CommonApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.HashMap;
import java.util.Map;
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<CommonApiResponse> handleResourceNotFound(ResourceNotFoundException ex) {
        return new ResponseEntity<>(CommonApiResponse.builder().success(false).message(ex.getMessage()).build(), HttpStatus.NOT_FOUND);
    }
    @ExceptionHandler(DuplicateBookingException.class)
    public ResponseEntity<CommonApiResponse> handleDuplicateBooking(DuplicateBookingException ex) {
        return new ResponseEntity<>(CommonApiResponse.builder().success(false).message(ex.getMessage()).build(), HttpStatus.CONFLICT);
    }
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<CommonApiResponse> handleIllegalArgument(IllegalArgumentException ex) {
        return new ResponseEntity<>(CommonApiResponse.builder().success(false).message(ex.getMessage()).build(), HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CommonApiResponse> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String field = error instanceof FieldError fieldError ? fieldError.getField() : error.getObjectName();
            errors.put(field, error.getDefaultMessage());
        });
        return new ResponseEntity<>(CommonApiResponse.builder().success(false).message("Validation Failed").data(errors).build(), HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(TokenRefreshException.class)
    public ResponseEntity<CommonApiResponse> handleTokenRefresh(TokenRefreshException ex) {
        return new ResponseEntity<>(CommonApiResponse.builder().success(false).message("Refresh token is invalid or expired.").build(), HttpStatus.UNAUTHORIZED);
    }
    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<CommonApiResponse> handleAccessDenied(org.springframework.security.access.AccessDeniedException ex) {
        return new ResponseEntity<>(CommonApiResponse.builder().success(false).message("Access denied.").build(), HttpStatus.FORBIDDEN);
    }
    @ExceptionHandler(org.springframework.security.core.AuthenticationException.class)
    public ResponseEntity<CommonApiResponse> handleAuthentication(org.springframework.security.core.AuthenticationException ex) {
        return new ResponseEntity<>(CommonApiResponse.builder().success(false).message("Invalid email or password.").build(), HttpStatus.UNAUTHORIZED);
    }
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<CommonApiResponse> handleIntegrityViolation(DataIntegrityViolationException ex) {
        logger.warn("Database constraint violation: {}", ex.getMostSpecificCause().getMessage());
        return new ResponseEntity<>(CommonApiResponse.builder().success(false).message("The request conflicts with existing data.").build(), HttpStatus.CONFLICT);
    }
    @ExceptionHandler({HttpMessageNotReadableException.class, HttpMediaTypeNotSupportedException.class})
    public ResponseEntity<CommonApiResponse> handleMalformedRequest(Exception ex) {
        return new ResponseEntity<>(CommonApiResponse.builder().success(false).message("Malformed request body or unsupported media type.").build(), HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<CommonApiResponse> handleGlobal(Exception ex) {
        logger.error("Unhandled exception occurred: ", ex);
        return new ResponseEntity<>(CommonApiResponse.builder().success(false).message("An internal server error occurred.").build(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
