package com.springtrade.portfolioservice.repository;

import com.springtrade.portfolioservice.entity.Holding;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HoldingRepository extends JpaRepository<Holding, String> {
    List<Holding> findAllByUserId(String userId);
}
