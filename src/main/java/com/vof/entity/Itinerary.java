package com.vof.entity;
import jakarta.persistence.*;
import lombok.*;
@Entity @Table(name = "itineraries") @Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Itinerary {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "package_id") private Package aPackage;
    private int dayNo; private String title; private String description;
}
