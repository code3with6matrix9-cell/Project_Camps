package com.vof.entity;
import com.vof.constant.BookingStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
@Entity @Table(name = "bookings"/*, indexes = {
        @Index(name = "idx_booking_owner", columnList = "created_by_user_id"),
        @Index(name = "idx_booking_package", columnList = "package_id")
}*/) @Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Booking {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(unique = true, nullable = false, updatable = false, length = 64) private String bookingId;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "package_id", nullable = false) private Package aPackage;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
@JoinColumn(name = "created_by_user_id", referencedColumnName = "id", nullable = false, updatable = false)
private User createdBy;
    @Column(nullable = false, length = 100) private String customerName;
    @Column(nullable = false, length = 254) private String email;
    @Column(nullable = false, length = 25) private String phone;
    private int adults; private int children; private LocalDate travelDate; private String specialRequest;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private BookingStatus status;
    @CreationTimestamp private LocalDateTime createdAt;
    @OneToOne(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true) private PaymentProof paymentProof;
}
