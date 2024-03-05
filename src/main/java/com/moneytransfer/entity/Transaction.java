package com.moneytransfer.entity;

import com.moneytransfer.enums.Currency;
import com.moneytransfer.enums.TransactionStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

/**
 * Entity that represents a financial transaction between two {@link Account} entities.
 */
@Entity
@Table(name = "transactions")
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class Transaction implements Serializable {
    @Id
    private UUID transactionId;

    private TransactionStatus transactionStatus;
    @ManyToOne
    @JoinColumn(name = "source_account_id", referencedColumnName = "accountId")
    private Account sourceAccount;

    @ManyToOne
    @JoinColumn(name = "target_account_id", referencedColumnName = "accountId")
    private Account targetAccount;

    private BigDecimal amount;

    private String message;

    private Currency currency;

    private HttpStatus httpStatus;

    @Override
    public int hashCode() {
        return Objects.hash(transactionId);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Transaction other = (Transaction) obj;
        return Objects.equals(transactionId, other.transactionId);
    }
}
