package com.davivienda.app.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;


@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;


    private LocalDate date;


    private BigDecimal amount;


    private String category;


    @Column(length = 20)
    private String type; // INCOME or EXPENSE


    @Column(columnDefinition = "TEXT")
    private String description;


    private Instant createdAt;


    @PrePersist
    public void prePersist() {
        createdAt = Instant.now();
    }
}