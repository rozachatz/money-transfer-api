package com.moneytransfer.repository;

import com.moneytransfer.entity.TransactionRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TransactionRequestRepository extends JpaRepository<TransactionRequest, UUID> {
}
