package com.moneytransactions.moneytransfer.dto;
import java.math.BigDecimal;
public class TransferRequest {
    private Long sourceAccountId;
    private Long targetAccountId;
    private BigDecimal amount;

    public TransferRequest(Long sourceAccountId, Long targetAccountId, BigDecimal amount) {
        this.sourceAccountId = sourceAccountId;
        this.targetAccountId = targetAccountId;
        this.amount = amount;
    }
    public Long getSourceAccountId() {
        return sourceAccountId;
    }

    public Long getTargetAccountId() {
        return targetAccountId;
    }

    public BigDecimal getAmount() {
        return amount;
    }
}
