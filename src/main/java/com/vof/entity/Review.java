package com.vof.entity;
import jakarta.persistence.*;
import lombok.*;
@Entity @Table(name = "reviews") @Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Review {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    private String name; private int rating; private String comment;
}
