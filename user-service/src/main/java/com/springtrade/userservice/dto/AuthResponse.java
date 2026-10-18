package com.springtrade.userservice.dto;

import com.springtrade.userservice.entity.UserSatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String userId;
    private String email;
    private String firstName;
    private String lastName;
    private BigDecimal walletBalance;
    private UserSatus status;
    private LocalDateTime createdAt;

    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";

    public AuthResponse(String id, String email, String firstName, String lastName, BigDecimal walletBalance, UserSatus status, LocalDateTime createdAt, String token, String refreshToken) {
        this.userId = id;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.walletBalance = walletBalance;
        this.status = status;
        this.createdAt = createdAt;
        this.accessToken = token;
        this.refreshToken = refreshToken;
    }
}
