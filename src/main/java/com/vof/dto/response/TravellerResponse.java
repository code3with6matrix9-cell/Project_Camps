package com.vof.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class TravellerResponse {
    private Long id;
    private String fullName;
    private Integer age;
    private String gender;
    private String phoneNumber;
    private String emergencyContact;
    private String idProofType;
    private String idProofNumber;
    private String medicalCondition;
    private LocalDateTime createdAt;
}