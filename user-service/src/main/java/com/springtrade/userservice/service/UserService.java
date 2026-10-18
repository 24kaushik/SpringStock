package com.springtrade.userservice.service;

import com.springtrade.userservice.config.UserProperties;
import com.springtrade.userservice.dto.AuthResponse;
import com.springtrade.userservice.dto.LoginRequest;
import com.springtrade.userservice.dto.RegisterRequest;
import com.springtrade.userservice.dto.UserResponse;
import com.springtrade.userservice.entity.User;
import com.springtrade.userservice.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserProperties userProperties;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String USER_REGISTERED_TOPIC = "user.registered";

    /**
     * Registers a new user in the system. It checks if the user already exists, encodes the password, saves the user to the database, and publishes a user registered event to Kafka.
     *
     * @param request the registration details provided by the user
     * @return an AuthResponse containing the user's information and authentication tokens
     */
    public AuthResponse registerUser(@Valid RegisterRequest request) {
        log.info("Registering user with email: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("User with email {} already exists", request.getEmail());
            throw new IllegalArgumentException("User with this email already exists");
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .walletBalance(
                        userProperties.initialBalance() != null ? userProperties.initialBalance() : BigDecimal.ZERO
                )
                .build();

        User savedUser = userRepository.save(user);
        log.info("User registered successfully with id: {}", savedUser.getId());

        // Publish user registered event to Kafka
        Map<String, Object> userRegisteredEvent = new HashMap<>();
        userRegisteredEvent.put("userId", savedUser.getId());
        userRegisteredEvent.put("email", savedUser.getEmail());
        userRegisteredEvent.put("firstName", savedUser.getFirstName());
        userRegisteredEvent.put("lastName", savedUser.getLastName());
        userRegisteredEvent.put("walletBalance", savedUser.getWalletBalance());

        kafkaTemplate.send(USER_REGISTERED_TOPIC, userRegisteredEvent);

        String token = generateToken(savedUser.getId(), savedUser.getEmail());
        String refreshToken = generateRefreshToken(savedUser.getId());

        return buildAuthResponse(savedUser, token, refreshToken);
    }

    /**
     * Gets a user by their ID.
     * Called by other services via Feign (internal)
     *
     * @param userId the ID of the user to retrieve
     * @return a UserResponse containing the user's information
     */
    public UserResponse getUserById(String userId) {
        log.info("Retrieving user with id: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User with id {} not found", userId);
                    return new IllegalArgumentException(
                            "User with this id does not exist"
                    );
                });

        log.info("User with id {} retrieved successfully", userId);

        return buildUserResponse(user);
    }

    /**
     * Adds funds to a user's wallet balance.
     *
     * @param userId the ID of the user to whom to add funds
     * @param amount the amount to add
     * @return a UserResponse containing the user's updated information
     */
    public UserResponse addFunds(String userId, BigDecimal amount) {
        log.info("Adding funds to user with id: {}", userId);

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("Invalid amount: {}. Amount must be greater than zero.", amount);
            throw new IllegalArgumentException("Amount must be greater than zero");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User with id {} not found", userId);
                    return new IllegalArgumentException(
                            "User with this id does not exist"
                    );
                });

        user.setWalletBalance(user.getWalletBalance().add(amount));
        userRepository.save(user);

        log.info("Funds added successfully. New wallet balance: {}", user.getWalletBalance());

        return buildUserResponse(user);
    }

    /**
     * Deducts funds from a user's wallet balance.
     *
     * @param userId the ID of the user from whom to deduct funds
     * @param amount the amount to deduct
     * @return a UserResponse containing the user's updated information
     */
    public UserResponse deductFunds(String userId, BigDecimal amount) {
        log.info("Deducting funds from user with id: {}", userId);

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("Invalid amount: {}. Amount must be greater than zero.", amount);
            throw new IllegalArgumentException("Amount must be greater than zero");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User with id {} not found", userId);
                    return new IllegalArgumentException(
                            "User with this id does not exist"
                    );
                });

        if (user.getWalletBalance().compareTo(amount) < 0) {
            log.warn("Insufficient funds for user with id: {}", userId);
            throw new IllegalArgumentException("Insufficient funds");
        }

        user.setWalletBalance(user.getWalletBalance().subtract(amount));
        userRepository.save(user);

        log.info("Funds deducted successfully. New wallet balance: {}", user.getWalletBalance());

        return buildUserResponse(user);
    }

    /**
     * Credits funds to a user's wallet balance. Called when selling a stock
     *
     * @param userId the ID of the user to whom to credit funds
     * @param amount the amount to credit
     * @return a UserResponse containing the user's updated information
     */
    public UserResponse creditFunds(String userId, BigDecimal amount) {
        log.info("Crediting funds to user with id: {}", userId);

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("Invalid amount: {}. Amount must be greater than zero.", amount);
            throw new IllegalArgumentException("Amount must be greater than zero");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User with id {} not found", userId);
                    return new IllegalArgumentException(
                            "User with this id does not exist"
                    );
                });

        user.setWalletBalance(user.getWalletBalance().add(amount));
        userRepository.save(user);

        log.info("Funds credited successfully. New wallet balance: {}", user.getWalletBalance());

        return buildUserResponse(user);
    }

    /**
     * Logs in a user
     *
     * @param request the login request containing the user's email and password
     * @return an AuthResponse containing the user's information and authentication tokens
     */
    public AuthResponse login(@Valid LoginRequest request) {
        log.info("Logging in user with email: {}", request.getEmail());

        User user = userRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.warn("User with email {} not found", request.getEmail());
                    return new IllegalArgumentException("User with this email does not exist");
                });

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword()
        )) {
            log.warn("Invalid password for user with email: {}", request.getEmail());
            throw new IllegalArgumentException("Invalid password");
        }

        log.info("User logged in successfully with email: {}", user.getEmail());

        // Generate authentication tokens
        String token = generateToken(user.getId(), user.getEmail());
        String refreshToken = generateRefreshToken(user.getId());

        return buildAuthResponse(user, token, refreshToken);
    }

    /**
     * Builds an AuthResponse object containing the user's information and authentication tokens.
     *
     * @param user the User object containing the user's information
     * @param token the authentication token for the user
     * @param refreshToken the refresh token for the user
     * @return an AuthResponse object containing the user's information and authentication tokens
     */
    private AuthResponse buildAuthResponse(User user, String token, String refreshToken) {
        return new AuthResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getWalletBalance(),
                user.getStatus(),
                user.getCreatedAt(),
                token,
                refreshToken
        );
    }

    /**
     * Returns a UserResponse for the given User.
     *
     * @param user the User for which to create a response
     * @return a UserResponse containing the user's information
     */
    private UserResponse buildUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getWalletBalance(),
                user.getStatus(),
                user.getCreatedAt()
        );
    }
}
