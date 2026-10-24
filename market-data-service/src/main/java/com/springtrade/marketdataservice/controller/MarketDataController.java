package com.springtrade.marketdataservice.controller;

import com.springtrade.marketdataservice.dto.StockPrice;
import com.springtrade.marketdataservice.entity.Stock;
import com.springtrade.marketdataservice.service.MarketDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/api/v1/market")
@Slf4j
@RequiredArgsConstructor
public class MarketDataController {
    private final MarketDataService marketDataService;

    @GetMapping("/stocks")
    public ResponseEntity<List<StockPrice>> allStockPrices() {
        List<StockPrice> stockPrices = marketDataService.getAllStockPrices();
        return ResponseEntity.ok(stockPrices);
    }
    @GetMapping("/stocks/list")
    public ResponseEntity<List<Stock>> getALlStocksList() {
        List<Stock> stocks = marketDataService.getAllStocks();
        return ResponseEntity.ok(stocks);
    }

    @GetMapping("/stocks/{symbol}")
    public ResponseEntity<java.lang.String> getStockPriceBySymbol(@PathVariable String symbol) {
        StockPrice stockPrice = marketDataService.getStockPriceBySymbol(symbol.toUpperCase());
        if (stockPrice != null) {
            return ResponseEntity.ok(stockPrice.toString());
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}

