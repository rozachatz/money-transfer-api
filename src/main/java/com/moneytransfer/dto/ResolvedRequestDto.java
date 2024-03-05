package com.moneytransfer.dto;

import com.moneytransfer.entity.Transaction;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;


public record ResolvedRequestDto(UUID requestId, Transaction transaction){
}
