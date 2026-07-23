package com.vof.entity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
@Entity
@Table(name = "packages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Package {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String slug;
    private String description;
    private Double price;
    private String duration;
    private String location;
    @CreationTimestamp
    private LocalDateTime createdAt;
    @OneToMany(mappedBy = "aPackage", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PackageImage> images = new ArrayList<>();
    @OneToMany(mappedBy = "aPackage", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Itinerary> itinerary = new ArrayList<>();
    @OneToOne(mappedBy = "aPackage", cascade = CascadeType.ALL, orphanRemoval = true)
    private Meal meal;
    @OneToMany(mappedBy = "aPackage")
    private List<Booking> bookings = new ArrayList<>();
}
