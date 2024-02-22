package com.moneytransfer.dto;

import com.moneytransfer.entity.Transaction;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@AllArgsConstructor
@Getter
public class ResolvedRequestDto {
    private UUID requestId;
    private Transaction transaction;
}
