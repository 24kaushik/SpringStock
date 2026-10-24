package com.springtrade.marketdataservice.repository;

import com.springtrade.marketdataservice.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockRepository extends JpaRepository<Stock, String> {
    List<Stock> findByActiveTrue();
}
