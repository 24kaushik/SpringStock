package com.springtrade.portfolioservice.controller;

import com.springtrade.portfolioservice.service.PortfolioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/portfolio")
@RequiredArgsConstructor
public class PortfolioController {
    private final PortfolioService portfolioService;

    /**
     * Get the portfolio for self.
     *
     * @param userId The ID of the user.
     * @return The user's portfolio.
     */
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getMyPortfolio(
            @RequestHeader(value = "X-User-Id", required = true) String userId
    ) {
        return ResponseEntity.ok(portfolioService.getPortfolioForUser(userId));
    }

    /**
     * Get the portfolio for some user.
     *
     * @param userId The ID of the user.
     * @return The user's portfolio.
     */
    @GetMapping("/{userId}")
    public ResponseEntity<Map<String, Object>> getPortfolio(
            @PathVariable String userId
    ) {
        return ResponseEntity.ok(portfolioService.getPortfolioForUser(userId));
    }
}
