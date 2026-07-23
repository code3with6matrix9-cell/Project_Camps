package com.vof.entity;
import jakarta.persistence.*;
import lombok.*;
@Entity @Table(name = "gallery_images") @Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class GalleryImage {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    private String imageUrl; private String caption;
}
