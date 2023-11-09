package com.moneytransfer.repository;

import com.moneytransfer.entity.Transaction;
import com.moneytransfer.entity.TransactionRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRequestRepository extends JpaRepository<TransactionRequest, UUID> {
}
