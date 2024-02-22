package com.moneytransfer.dto;

import com.moneytransfer.entity.Transaction;
import com.moneytransfer.enums.RequestStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@AllArgsConstructor
@Getter
public class RequestUpdateDto {
    private UUID requestId;
    private RequestStatus requestStatus;
    private Transaction transaction;
}
