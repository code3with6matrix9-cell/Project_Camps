package com.vof.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TravellerRequest {
    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotNull(message = "Age is required")
    private Integer age;

    @NotBlank(message = "Gender is required")
    private String gender;

    @NotBlank(message = "Phone number is required")
    private String phoneNumber;

    @NotBlank(message = "Emergency contact is required")
    private String emergencyContact;

    private String idProofType;
    private String idProofNumber;
    private String medicalCondition;
}