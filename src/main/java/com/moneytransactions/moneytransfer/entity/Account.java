package com.moneytransactions.moneytransfer.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity //JPA entity
@Table(name = "accounts")
@Getter
@Setter
public class Account {
    @Version //optimistic
    protected int version;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id")
    private Long id; // PRIMARY KEY
    private BigDecimal balance;
    private String currency; // TODO: Enum for different currencies
    private LocalDateTime createdAt;

    public Account(BigDecimal balance, String currency, LocalDateTime createdAt) {
        this.balance = balance;
        this.currency = currency;
        this.createdAt = LocalDateTime.now();
    }

    private Account() {
    }

    public void credit(BigDecimal amount) {
        this.balance = balance.add(amount);
    }

    public void debit(BigDecimal amount) {
        this.balance = balance.subtract(amount);
    }
    // TODO: Currency Exchange Mechanism (internal storage or API)
}
