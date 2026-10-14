package com.springtrade.userservice.service;

import com.springtrade.userservice.dto.AuthResponse;
import com.springtrade.userservice.dto.RegisterRequest;
import com.springtrade.userservice.dto.UserResponse;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;

import java.math.BigInteger;

@Service
public class UserService {
    public AuthResponse registerUser(@Valid RegisterRequest request) {
        return null;
    }

    public AuthResponse login(@Valid RegisterRequest request) {
        return null;
    }

    public UserResponse getUserById(String userId) {
        return null;
    }

    public UserResponse addFunds(String userId, BigInteger amount) {
        return null;
    }
}
