package com.megha.bank.repository;

import com.megha.bank.model.Trade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TradeRepository extends JpaRepository<Trade, Long> {
    Optional<Trade> findByTradeRef(String tradeRef);
}
