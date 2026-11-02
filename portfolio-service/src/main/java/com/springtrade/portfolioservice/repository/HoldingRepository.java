package com.springtrade.portfolioservice.repository;

import com.springtrade.portfolioservice.entity.Holding;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HoldingRepository extends JpaRepository<Holding, String> {
    List<Holding> findAllByUserId(String userId);

    Optional<Holding> findByUserIdAndSymbol(String userId, String symbol);
}
