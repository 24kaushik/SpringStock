package com.springtrade.orderservice.controller;

import com.springtrade.orderservice.dto.OrderRequest;
import com.springtrade.orderservice.entity.Order;
import com.springtrade.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * Place an order for a stock.
     *
     * @param userId  The ID of the user placing the order, passed in the request header.
     * @param request The order request details, including stock symbol, order type, and quantity.
     * @return ResponseEntity containing the created Order object.
     */
    @PostMapping("/")
    public ResponseEntity<Order> placeOrder(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody OrderRequest request
    ) {
        request.setUserId(userId); // Override
        return ResponseEntity.ok(orderService.createOrder(request));
    }


    /**
     * Get an order by its ID.
     * @param userId The ID of the user making the request, passed in the request header.
     * @param orderId The ID of the order to retrieve, passed as a path variable.
     * @return ResponseEntity containing the Order object if found and authorized, or a 403 Forbidden status if the user is not authorized to access the order.
     */
    @GetMapping("/orders/{orderId}")
    public ResponseEntity<Order> getOrder(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String orderId
    ) {
        Order order = orderService.getOrderById(orderId);
        // Security check: Ensure the user is authorized to access this order
        if (!order.getUserId().equals(userId)) {
            return ResponseEntity.status(403).build(); // Forbidden
        }
        return ResponseEntity.ok(order);
    }

    /**
     * Get all orders for the authenticated user.
     * @param userId The ID of the user making the request, passed in the request header.
     * @return ResponseEntity containing a list of Order objects associated with the user.
     */
    @GetMapping("/orders")
    public ResponseEntity<List<Order>> getMyOrders(
            @RequestHeader("X-User-Id") String userId
    ) {
        return ResponseEntity.ok(orderService.getOrdersByUserId(userId));
    }

}
