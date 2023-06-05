package com.moneytransactions.moneytransfer.repository;

import com.moneytransactions.moneytransfer.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
}
