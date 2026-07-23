package com.vof.entity;
import jakarta.persistence.*;
import lombok.*;
@Entity @Table(name = "faqs") @Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Faq {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    private String question; private String answer;
}
