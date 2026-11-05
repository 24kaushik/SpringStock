package com.springtrade.notificationservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
public class NotificationService {
    @KafkaListener(topics = "order.executed")
    public void consumeOrderExecuted(
            @Payload Map<String, Object> payload
    ) {
        try {
            String userId = (String) payload.get("userId");
            String type = (String) payload.get("type");
            String symbol = (String) payload.get("symbol");
            Object quantity = payload.get("quantity");
            Object price = payload.get("price");
            Object totalAmount = payload.get("totalAmount");

            sendNotification(userId,
                    "BUY".equalsIgnoreCase(type) ?
                            "Order Executed - Buy" :
                            "Order Executed - Sell",
                    String.format("Your order to %s %s %s at $%s has been executed. Total Amount: $%s",
                            type.toLowerCase(),
                            symbol,
                            quantity,
                            price,
                            totalAmount
                    )
            );

        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    @KafkaListener(topics = "order.failed")
    private void consumeOrderFailed(
            @Payload Map<String, Object> payload
    ) {
        try {
            String userId = (String) payload.get("userId");
            String type = (String) payload.get("type");
            String symbol = (String) payload.get("symbol");
            String reason = (String) payload.get("failureReason");

            sendNotification(userId,
                    "BUY".equalsIgnoreCase(type) ?
                            "Order Failed - Buy" :
                            "Order Failed - Sell",
                    String.format("Your order to %s %s has failed. Reason: %s",
                            type.toLowerCase(),
                            symbol,
                            reason
                    )
            );

        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    @KafkaListener(topics = "user.registered")
    public void consumeUserRegistered(
            @Payload Map<String, Object> payload
    ) {
        try {
            String userId = (String) payload.get("userId");
            String firstName = (String) payload.get("firstName");
            Object walletBalance = payload.get("walletBalance");

            sendNotification(userId,
                    "Welcome to SpringTrade",
                    String.format("Welcome %s! Your wallet balance is $%s.",
                            firstName,
                            walletBalance

                    )
            );

        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    private void sendNotification(String userId, String title, String message) {
        // Will implement Email notification later. For now, just log the notification.

        log.info("----------------------------------------------------------");
        log.info("-----------------------NOTIFICATION-----------------------");
        log.info("User ID: {}", userId);
        log.info("Title: {}", title);
        log.info("Message: {}", message);
        log.info("----------------------------------------------------------");
    }
}
