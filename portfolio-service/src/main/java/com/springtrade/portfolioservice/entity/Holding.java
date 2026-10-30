package com.springtrade.portfolioservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
@Entity
@Table(
        name = "holdings",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "symbol"})
)
public class Holding {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(nullable = false)
    private String symbol;

    @Column(nullable = false)
    private String companyName;

    private int quantity;

    @Column(scale = 2, precision = 15, nullable = false)
    private BigDecimal averageBuyPrice;

    @Column(scale = 2, precision = 15, nullable = false)
    private BigDecimal totalInvested;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
