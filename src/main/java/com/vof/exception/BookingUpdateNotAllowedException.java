package com.vof.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BookingUpdateNotAllowedException extends RuntimeException {
    public BookingUpdateNotAllowedException(String message) {
        super(message);
    }
}
