package com.vof.entity;
import jakarta.persistence.*;
import lombok.*;
@Entity @Table(name = "contact_messages") @Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ContactMessage {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    private String name; private String email; private String phone; private String message;
}
