package com.vof.entity;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.HashSet;
import java.util.Set;
@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(columnNames = "username"),
        @UniqueConstraint(columnNames = "email"),
        @UniqueConstraint(columnNames = "mobile_number")
})
@Getter
@Setter
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 254)
    private String username;

    @Column(nullable = false, length = 254)
    private String email;

    @Column(name = "mobile_number", nullable = false, length = 16)
    private String mobileNumber;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();
    public User(String email, String mobileNumber, String password) {
        // username is retained only for compatibility with the existing database schema.
        // It is always the normalized email; clients never register or log in with a username.
        this.username = email;
        this.email = email;
        this.mobileNumber = mobileNumber;
        this.password = password;
    }
}
