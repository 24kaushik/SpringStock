package com.springtrade.orderservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "orders")
public class    Order {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, updatable = false)
    private String userId;

    @Column(nullable = false, updatable = false)
    private String stockSymbol;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private OrderType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private OrderStatus status;

    @Column(nullable = false, updatable = false)
    private int quantity;

    @Column(nullable = false, updatable = false, precision = 15, scale = 2)
    private BigDecimal price;

    @Column(nullable = false, updatable = false, precision = 15, scale = 2)
    private BigDecimal totalAmount;

    private String failureReason;

    @CreationTimestamp
    private LocalDateTime createdAt;

    private LocalDateTime executedAt;
}
