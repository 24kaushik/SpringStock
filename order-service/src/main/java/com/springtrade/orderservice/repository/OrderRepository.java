package com.springtrade.orderservice.repository;

import com.springtrade.orderservice.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, String> {
    List<Order> findByUserId(String userId);

    List<Order> findByUserIdOrderByCreatedAtDesc(String userId);
}
