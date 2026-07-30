package com.vof.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "travellers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Traveller {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Column(nullable = false, length = 100)
    private String fullName;

    @Column(nullable = false)
    private Integer age;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Gender gender;

    @Column(nullable = false, length = 25)
    private String phoneNumber;

    @Column(nullable = false, length = 100)
    private String emergencyContact;

    @Column(length = 50)
    private String idProofType;

    @Column(length = 64)
    private String idProofNumber;

    @Column(length = 500)
    private String medicalCondition;

    @CreationTimestamp
    private LocalDateTime createdAt;

    public enum Gender {
        MALE, FEMALE, OTHER
    }
}