package com.moneytransfer.entity;

import com.moneytransfer.dto.NewTransferDto;
import com.moneytransfer.enums.RequestStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "requests")
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class Request implements Serializable {
    @Id
    private UUID requestId;
    private RequestStatus requestStatus;
    private BigDecimal amount;
    private UUID sourceAccountId;
    private UUID targetAccountId;

    @OneToOne
    @JoinColumn(name = "transaction_id", referencedColumnName = "transactionId")
    private Transaction transaction;

    public Request(UUID requestId, RequestStatus requestStatus, Transaction transaction) {
        this.requestId = requestId;
        this.requestStatus = requestStatus;
        this.transaction = transaction;
        this.amount = transaction.getAmount();
        this.sourceAccountId = transaction.getSourceAccount().getAccountId();
        this.targetAccountId = transaction.getTargetAccount().getAccountId();
    }

    public Request(UUID requestId, RequestStatus requestStatus, BigDecimal amount, UUID sourceAccountId, UUID targetAccountId) {
        this.requestId = requestId;
        this.requestStatus = requestStatus;
        this.amount = amount;
        this.sourceAccountId = sourceAccountId;
        this.targetAccountId = targetAccountId;
    }

    public NewTransferDto toNewTransferDto() {
        return new NewTransferDto(this.sourceAccountId, this.targetAccountId, this.amount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(requestId);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Request other = (Request) obj;
        return Objects.equals(requestId, other.requestId);
    }
}
