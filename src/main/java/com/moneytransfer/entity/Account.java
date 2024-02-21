package com.moneytransfer.entity;

import com.moneytransfer.enums.Currency;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Bank Account entity
 */
@Entity
@Table(name = "accounts")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class Account {
    @Id
    private UUID accountId;
    protected String ownerName;
    @Version
    protected int version;
    private BigDecimal balance;
    @Enumerated(EnumType.STRING)
    private Currency currency;
    private LocalDateTime createdAt;

    public void credit(BigDecimal amount) {
        this.balance = balance.add(amount);
    }

    public void debit(BigDecimal amount) {
        this.balance = balance.subtract(amount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountId);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Account other = (Account) obj;
        return Objects.equals(accountId, other.accountId);
    }
}
