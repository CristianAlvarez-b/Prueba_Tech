package com.davivienda.app.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;


@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(nullable = false, unique = true)
    private String username;


    @Column(nullable = false, unique = true)
    private String email;


    @Column(nullable = false)
    private String password;


    private String roles; // e.g. ROLE_USER,ROLE_ADMIN


    private Instant createdAt;


    @PrePersist
    public void prePersist() {
        createdAt = Instant.now();
    }
}