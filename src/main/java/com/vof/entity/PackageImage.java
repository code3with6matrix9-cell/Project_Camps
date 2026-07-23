package com.vof.entity;
import jakarta.persistence.*;
import lombok.*;
@Entity @Table(name = "package_images") @Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class PackageImage {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "package_id") private Package aPackage;
    private String imageUrl;
}
