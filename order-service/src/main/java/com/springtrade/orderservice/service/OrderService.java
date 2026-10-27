package com.springtrade.orderservice.service;

import com.springtrade.orderservice.client.MarketDataClient;
import com.springtrade.orderservice.client.UserServiceClient;
import com.springtrade.orderservice.dto.OrderRequest;
import com.springtrade.orderservice.entity.Order;
import com.springtrade.orderservice.entity.OrderStatus;
import com.springtrade.orderservice.entity.OrderType;
import com.springtrade.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final MarketDataClient marketDataClient;
    private final UserServiceClient userServiceClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private final static String ORDER_EXECUTED_TOPIC = "order.executed";
    private final static String ORDER_FAILED_TOPIC = "order.failed";

    public Order createOrder(OrderRequest request) {
        log.info("Creating order for user: {}, stock: {}, type: {}, quantity: {}",
                request.getUserId(), request.getStockSymbol(), request.getType(), request.getQuantity());

        Map<String, Object> stockData = marketDataClient.getStockPriceBySymbol(request.getStockSymbol());

        BigDecimal currentPrice = new BigDecimal(stockData.get("price").toString());
        BigDecimal totalAmount = currentPrice.multiply(BigDecimal.valueOf(request.getQuantity()));

        Order order = new Order();
        order.setUserId(request.getUserId());
        order.setStockSymbol(request.getStockSymbol());
        order.setType(request.getType());
        order.setQuantity(request.getQuantity());
        order.setPrice(currentPrice);
        order.setTotalAmount(totalAmount);
        order.setStatus(OrderStatus.PENDING);

        Order savedOrder = orderRepository.save(order);

        log.info("Order created with ID: {}", savedOrder.getId());

        try {
            if (request.getType() == OrderType.BUY) {
                userServiceClient.removeFundsFromUser(
                        request.getUserId(),
                        totalAmount
                );
            } else {
                userServiceClient.creditFunds(
                        request.getUserId(),
                        totalAmount
                );
            }
            savedOrder.setStatus(OrderStatus.EXECUTED);
            savedOrder.setExecutedAt(LocalDateTime.now());

            log.info("Order executed for user: {}, order ID: {}", request.getUserId(), savedOrder.getId());

            publishOrderEvent(ORDER_EXECUTED_TOPIC, savedOrder, null);
        } catch (Exception e) {
            savedOrder.setStatus(OrderStatus.FAILED);
            savedOrder.setFailureReason(e.getMessage());

            log.error("Error processing order for user: {}, order ID: {}, error: {}",
                    request.getUserId(), savedOrder.getId(), e.getMessage());

            publishOrderEvent(ORDER_FAILED_TOPIC, savedOrder, e.getMessage());
        } finally {
            orderRepository.save(savedOrder);
        }

        return savedOrder;
    }

    private void publishOrderEvent(String topic, Order order, String reason) {
        Map<String, Object> event = new HashMap<>();
        event.put("orderId", order.getId());
        event.put("userId", order.getUserId());
        event.put("stockSymbol", order.getStockSymbol());
        event.put("type", order.getType());
        event.put("quantity", order.getQuantity());
        event.put("price", order.getPrice());
        event.put("totalAmount", order.getTotalAmount());
        event.put("status", order.getStatus());
        if (reason != null) event.put("failureReason", reason);

        kafkaTemplate.send(topic, event);
        log.info("Published order event to topic {}: {}", topic, event);
    }

    public Order getOrderById(String orderId) {
        return orderRepository.findById(orderId).orElseThrow(() -> new IllegalArgumentException("Order not found"));
    }

    public List<Order> getOrdersByUserId(String userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
}
