package com.springtrade.marketdataservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "stocks")
public class Stock {
    @Id
    @Column(nullable = false, unique = true)
    private String symbol;

    @Column(nullable = false)
    private String companyName;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal initialPrice;

    @Column(nullable = false)
    private String exchange; // NSE, NASDAQ, NYSE, etc.

    @Column(nullable = false)
    private String currency; // USD, INR, EUR, etc.

    @Column(nullable = false)
    private Boolean isActive = true;
}
