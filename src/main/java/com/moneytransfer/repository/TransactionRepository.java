package com.moneytransfer.repository;

import com.moneytransfer.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    Optional<List<Transaction>> findByAmountBetween(BigDecimal minAmount, BigDecimal maxAmount);

}
