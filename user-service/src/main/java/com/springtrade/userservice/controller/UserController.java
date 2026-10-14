package com.springtrade.userservice.controller;

import com.springtrade.userservice.common.response.ApiResponse;
import com.springtrade.userservice.dto.AuthResponse;
import com.springtrade.userservice.dto.RegisterRequest;
import com.springtrade.userservice.dto.UserResponse;
import com.springtrade.userservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;

@RestController
@RequestMapping("/api/v1/users")
@Slf4j
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> registerUser(
            @Valid @RequestBody RegisterRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(
                                "User registered successfully",
                                userService.registerUser(request)
                        )
                );
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> loginUser(
            @Valid @RequestBody RegisterRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(
                        ApiResponse.success(
                                "User logged in successfully",
                                userService.login(request)
                        )
                );
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getProfile(
            @RequestHeader("X-User-Id") String userId
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(
                        ApiResponse.success(
                                "User profile retrieved successfully",
                                userService.getUserById(userId)
                        )
                );
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(
            @PathVariable String userId
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(
                        ApiResponse.success(
                                "User profile retrieved successfully",
                                userService.getUserById(userId)
                        )
                );
    }

    @PostMapping("/{userId}/funds/add")
    public ResponseEntity<ApiResponse<UserResponse>> addFundsToUser(
            @PathVariable String userId,
            @RequestParam BigInteger amount
            ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(
                        ApiResponse.success(
                                "Funds added to user successfully",
                                userService.addFunds(userId, amount)
                        )
                );
    }
}
