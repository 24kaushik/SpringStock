package com.springtrade.marketdataservice.service;

import com.springtrade.marketdataservice.dto.StockPrice;
import com.springtrade.marketdataservice.entity.Stock;
import com.springtrade.marketdataservice.repository.StockRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class MarketDataService {

    private final StockRepository stockRepository;

    private final Map<String, StockPrice> stockPriceCache = new ConcurrentHashMap<>();
    private final RedisTemplate<String, Object> redisTemplate;
    private final SimpMessagingTemplate messagingTemplate;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String STOCK_PRICE_KEY_PREFIX = "stock:price:";
    private static final String STOCK_PRICE_UPDATE_TOPIC = "stock.price.updated";

    /**
     * Initialize the stock price cache with data from the repository.
     */
    @PostConstruct
    public void initializeStockPriceCache() {
        log.info("Initializing stock price cache...");

        List<Stock> stocks = stockRepository.findByActiveTrue();

        if (stocks.isEmpty()) {
            log.warn("No active stocks found in the repository.");
            return;
        }

        stocks.forEach(stock -> {
            stockPriceCache.put(
                    stock.getSymbol(),
                    new StockPrice(
                            stock.getSymbol(),
                            stock.getCompanyName(),
                            stock.getExchange(),
                            stock.getCurrency(),
                            stock.getInitialPrice(), // price
                            BigDecimal.ZERO, // change
                            BigDecimal.ZERO, // changePercent
                            stock.getInitialPrice(), // high
                            stock.getInitialPrice(), // low
                            stock.getInitialPrice(), // open
                            0L, // volume
                            LocalDateTime.now()
                    ));
            log.info("Added stock price for symbol: {} at price {}", stock.getSymbol(), stock.getInitialPrice());
        });

        log.info("Stock price cache initialized with {} entries.", stockPriceCache.size());
    }

    /**
     * Simulate stock price updates at a fixed interval defined in the application properties.
     */
    @Scheduled(fixedRateString = "${market.price-update-interval-ms}")
    public void updatePrices() {
        stockPriceCache.forEach((symbol, stockPrice) -> {

            // Random change between -5% and +5%
            double randomChangePercent = (Math.random() - 0.5) / 10.0;

            BigDecimal change = stockPrice.getPrice().multiply(BigDecimal.valueOf(randomChangePercent));
            BigDecimal changePercent = change.divide(stockPrice.getPrice(), 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
            BigDecimal newPrice = stockPrice.getPrice().add(change).setScale(2, RoundingMode.HALF_UP);

            if (newPrice.compareTo(BigDecimal.ONE) < 0) return; // Prevent price from going below 1

            stockPrice.setPrice(newPrice);
            stockPrice.setChange(change);
            stockPrice.setChangePercent(changePercent);
            stockPrice.setHigh(stockPrice.getHigh().max(newPrice));
            stockPrice.setLow(stockPrice.getLow().min(newPrice));
            stockPrice.setTimestamp(LocalDateTime.now());

            // Cache in redis
            redisTemplate.opsForValue().set(STOCK_PRICE_KEY_PREFIX + symbol, stockPrice);

            // Notify clients
            messagingTemplate.convertAndSend("/topic/prices/" + symbol, stockPrice);

            // Publish to Kafka
            Map<String, Object> priceUpdatedEvent = new HashMap<>();
            priceUpdatedEvent.put("symbol", symbol);
            priceUpdatedEvent.put("price", newPrice);
            priceUpdatedEvent.put("timestamp", LocalDateTime.now());

            kafkaTemplate.send(STOCK_PRICE_UPDATE_TOPIC, symbol, priceUpdatedEvent);

        });

        log.debug("Updated stock prices for {} symbols.", stockPriceCache.size());
    }

    /**
     * Get all stock prices from the cache.
     * @return List of StockPrice objects
     */
    public List<StockPrice> getAllStockPrices() {
        return stockPriceCache.values().stream().toList();
    }

    /**
     * Get stock price by symbol. First checks Redis cache, then falls back to in-memory cache.
     * @param symbol The stock symbol
     * @return The stock price
     */
    public StockPrice getStockPriceBySymbol(String symbol) {
        Object cachedPrice = redisTemplate.opsForValue().get(STOCK_PRICE_KEY_PREFIX + symbol);
        if (cachedPrice != null) {
            return (StockPrice) cachedPrice;
        }

        // Fallback
        StockPrice stockPrice = stockPriceCache.get(symbol);
        if (stockPrice == null) {
            throw new IllegalArgumentException("Stock symbol not found: " + symbol);
        }
        return stockPrice;
    }

    /**
     * Get all stocks from the repository.
     * @return List of Stock entities
     */
    public List<Stock> getAllStocks() {
        return stockRepository.findByActiveTrue();
    }
}
