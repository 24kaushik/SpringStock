package com.springtrade.portfolioservice.service;

import com.springtrade.portfolioservice.entity.Holding;
import com.springtrade.portfolioservice.repository.HoldingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class PortfolioEventConsumer {

    private final HoldingRepository holdingRepository;

    /**
     * Consumes an order executed event and updates the user's portfolio accordingly.
     * Depending on the order type (BUY or SELL), it either adds to or removes from the user's holdings.
     *
     * @param orderExecutedEvent A map containing details of the executed order, including userId, symbol, type, quantity, and price.
     */
    @KafkaListener(topics = "order.executed")
    public void consumeOrderExecuted(
            @Payload Map<String, Object> orderExecutedEvent
    ) {
        try {
            String userId = (String) orderExecutedEvent.get("userId");
            String symbol = (String) orderExecutedEvent.get("symbol");
            String type = (String) orderExecutedEvent.get("type");
            int quantity = Integer.parseInt(orderExecutedEvent.get("quantity").toString());

            BigDecimal price = new BigDecimal(orderExecutedEvent.get("price").toString());

            log.info("Updating portfolio - userId: {}, symbol: {} quantity: {}, price: {}", userId, symbol, quantity, price);

            if ("BUY".equalsIgnoreCase(type)) {
                log.info("Processing BUY order for userId: {}, symbol: {}, quantity: {}, price: {}", userId, symbol, quantity, price);
                addHolding(userId, symbol, quantity, price);
            } else if ("SELL".equalsIgnoreCase(type)) {
                log.info("Processing SELL order for userId: {}, symbol: {}, quantity: {}, price: {}", userId, symbol, quantity, price);
                removeHolding(userId, symbol, quantity, price);
            } else {
                log.warn("Unknown order type received: {}", type);
            }
        } catch (NumberFormatException e) {
            log.error("Error parsing order executed event: {}", e.getMessage(), e);
        }
    }

    /**
     * Adds a holding to the user's portfolio based on the executed order details.
     * Updates the quantity and average buy price of the holding if it already exists, or creates a new holding if it doesn't.
     *
     * @param userId   The ID of the user whose portfolio is being updated.
     * @param symbol   The stock symbol of the holding being added.
     * @param quantity The quantity of the stock being added to the holding.
     * @param price    The price at which the stock was bought, used to calculate the new average buy price.
     */
    private void addHolding(String userId, String symbol, int quantity, BigDecimal price) {
        Holding holding = holdingRepository.findByUserIdAndSymbol(userId, symbol)
                .orElse(null);

        if (holding == null) {
            holding = new Holding();
            holding.setUserId(userId);
            holding.setSymbol(symbol);
            holding.setCompanyName(symbol);
            holding.setQuantity(quantity);
            holding.setAverageBuyPrice(price);
            holding.setTotalInvested(price.multiply(BigDecimal.valueOf(quantity)));
        } else {
            int newQuantity = holding.getQuantity() + quantity;
            BigDecimal newTotalInvested = holding.getTotalInvested().add(price.multiply(BigDecimal.valueOf(quantity)));
            BigDecimal newAverageBuyPrice = newTotalInvested.divide(BigDecimal.valueOf(newQuantity), 2, RoundingMode.HALF_UP);

            holding.setQuantity(newQuantity);
            holding.setTotalInvested(newTotalInvested);
            holding.setAverageBuyPrice(newAverageBuyPrice);
        }

        holdingRepository.save(holding);

        log.info("Holding updated for userId: {}, symbol: {}, new quantity: {}, new average buy price: {}", userId, symbol, holding.getQuantity(), holding.getAverageBuyPrice());
    }

    private void removeHolding(String userId, String symbol, int quantity, BigDecimal price) {
        Holding holding = holdingRepository.findByUserIdAndSymbol(userId, symbol)
                .orElseThrow(() -> new IllegalArgumentException("Holding not found for userId: " + userId + ", symbol: " + symbol));

        if (holding.getQuantity() < quantity) {
            throw new IllegalArgumentException("Insufficient quantity to sell for userId: " + userId + ", symbol: " + symbol);
        }

        int newQuantity = holding.getQuantity() - quantity;

        if (newQuantity == 0) {
            holdingRepository.delete(holding);
            log.info("Holding removed for userId: {}, symbol: {}", userId, symbol);
        } else {
            BigDecimal newTotalInvested = holding.getAverageBuyPrice().multiply(BigDecimal.valueOf(newQuantity));
            holding.setQuantity(newQuantity);
            holding.setTotalInvested(newTotalInvested);
            holdingRepository.save(holding);
            log.info("Holding updated for userId: {}, symbol: {}, new quantity: {}, new total invested: {}", userId, symbol, newQuantity, newTotalInvested);
        }
    }
}
