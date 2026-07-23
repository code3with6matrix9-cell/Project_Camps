package com.vof.entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;
@Entity(name = "refreshtoken") @Getter @Setter
public class RefreshToken {
    @Id @GeneratedValue(strategy = GenerationType.AUTO) private long id;
    @OneToOne(optional = false) @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false, unique = true) private User user;
    @Column(nullable = false, unique = true) private String token;
    @Column(nullable = false) private Instant expiryDate;
}
