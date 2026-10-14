package com.springtrade.userservice.dto;

import com.springtrade.userservice.entity.UserSatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private String userId;
    private String email;
    private String firstName;
    private String lastName;
    private BigDecimal walletBalance;
    private UserSatus status;
    private LocalDateTime createdAt;
}
