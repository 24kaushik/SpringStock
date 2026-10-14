package com.springtrade.userservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;

@ConfigurationProperties(prefix = "app.user")
public record UserProperties(BigDecimal initialBalance) {
}
