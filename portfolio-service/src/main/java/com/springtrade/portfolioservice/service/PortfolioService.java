package com.springtrade.portfolioservice.service;

import com.springtrade.portfolioservice.client.MarketDataClient;
import com.springtrade.portfolioservice.entity.Holding;
import com.springtrade.portfolioservice.repository.HoldingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class PortfolioService {
    private final HoldingRepository holdingRepository;
    private final MarketDataClient marketDataClient;

    /**
     * Fetches the portfolio for a given user, including details about each holding and overall portfolio performance.
     * @param userId The ID of the user whose portfolio is to be fetched.
     * @return A map containing the user's portfolio details, including individual holdings, total invested amount, current value, total profit/loss, and profit/loss percentage.
     */
    public Map<String, Object> getPortfolioForUser(String userId) {
        List<Holding> holdings = holdingRepository.findAllByUserId(userId);

        List<Map<String, Object>> portfolioItems = new ArrayList<>();

        BigDecimal totalInvested = BigDecimal.ZERO;
        BigDecimal currentValue = BigDecimal.ZERO;

        for (Holding holding : holdings) {
            try {
                Map<String, Object> stockData = marketDataClient.getStockPriceBySymbol(holding.getSymbol());

                BigDecimal currentPrice = new BigDecimal(stockData.get("price").toString());
                BigDecimal holdingValue = currentPrice.multiply(BigDecimal.valueOf(holding.getQuantity()));
                BigDecimal invested = holding.getTotalInvested();
                BigDecimal pnl = holdingValue.subtract(invested);
                BigDecimal pnlPercentage = pnl.divide(invested, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP);

                Map<String, Object> portfolioItem = Map.of(
                        "symbol", holding.getSymbol(),
                        "quantity", holding.getQuantity(),
                        "avgBuyPrice", holding.getAverageBuyPrice(),
                        "currentPrice", currentPrice,
                        "holdingValue", holdingValue,
                        "totalInvested", invested,
                        "pnl", pnl,
                        "pnlPercentage", pnlPercentage,
                        "isProfit", pnl.compareTo(BigDecimal.ZERO) > 0

                );

                portfolioItems.add(portfolioItem);
                totalInvested = totalInvested.add(invested);
                currentValue = currentValue.add(holdingValue);
            } catch (Exception e) {
                log.error("Error occurred while processing holding for user: {}", e.getMessage());
            }
        }

        BigDecimal totalPnL = currentValue.subtract(totalInvested);
        BigDecimal totalPnLPercentage = totalInvested.compareTo(BigDecimal.ZERO) > 0 ? totalPnL.divide(totalInvested, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;

        return Map.of(
                "userId", userId,
                "holdings", portfolioItems,
                "totalInvested", totalInvested,
                "currentValue", currentValue,
                "totalPnL", totalPnL,
                "totalPnLPercentage", totalPnLPercentage,
                "isProfit", totalPnL.compareTo(BigDecimal.ZERO) > 0
        );

    }
}
