package com.springtrade.orderservice.dto;

import com.springtrade.orderservice.entity.OrderType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderRequest {
    private String userId;

    @NotBlank(message = "Stock symbol is required")
    private String stockSymbol;

    @NotBlank(message = "Order type is required (BUY or SELL)")
    private OrderType type; // BUY or SELL

    @Min(value = 1, message = "Quantity is required and must be greater than 0")
    private int quantity;
}
