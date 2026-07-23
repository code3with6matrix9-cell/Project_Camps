package com.vof.util;
import org.springframework.stereotype.Component;
import java.util.UUID;
@Component
public class BookingIdGenerator {
    public String generateBookingId() {
        return "BK-" + UUID.randomUUID();
    }
}
