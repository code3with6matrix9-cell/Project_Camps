package com.vof.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateBookingRequest {
    @Size(max = 100)
    private String customerName;

    @Size(max = 25)
    private String phone;

    // Assuming these fields might be part of an "address" or "profile" object in a real app,
    // but adding them directly to the request for this feature.
    private String alternateMobileNumber;
    private String emergencyContact;
    private Integer age;
    private String gender;
    private String address;
    private String pickupPoint;

    @Size(max = 1000)
    private String specialRequest;
}
