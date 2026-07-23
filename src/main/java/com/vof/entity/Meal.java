package com.vof.entity;
import jakarta.persistence.*;
import lombok.*;
@Entity @Table(name = "meals") @Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Meal {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @OneToOne(fetch = FetchType.LAZY) @JoinColumn(name = "package_id") private Package aPackage;
    private String breakfast; private String lunch; private String dinner;
}
