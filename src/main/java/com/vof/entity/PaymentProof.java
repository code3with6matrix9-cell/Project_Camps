package com.vof.entity;
import com.vof.constant.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity @Table(name = "payment_proofs", indexes = @Index(name = "idx_payment_proof_utr", columnList = "utr")) @Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class PaymentProof {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @OneToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "booking_id", nullable = false, unique = true) private Booking booking;
    @Column(nullable = false, unique = true, length = 64) private String utr;
    @Column(nullable = false) private BigDecimal amount;
    @Column(nullable = false, length = 2048) private String screenshotUrl;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30) private PaymentStatus status;
}
