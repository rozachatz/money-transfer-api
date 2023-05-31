package com.moneytransactions.moneytransfer.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity //JPA entity
@Table(name = "accounts")
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id")
    private Long id; // PRIMARY KEY
    private BigDecimal balance;
    private String currency; // TODO: Enum for different currencies
    private LocalDateTime createdAt;

    /*
     * CONSTRUCTORS
     */
    public Account(BigDecimal balance, String currency) {
        this.balance = balance;
        this.currency = currency;
        this.createdAt = LocalDateTime.now();
    }

    private Account() {

    }

    /*
     * GETTERS AND SETTERS
     **/
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    /*
     * FINANCIAL TRANSACTION METHODS
     */
    public void credit(BigDecimal amount) {
        this.balance = balance.add(amount);
    }

    public void debit(BigDecimal amount) {
        this.balance = balance.subtract(amount);
    }
    // TODO: Currency Exchange Mechanism (internal storage or API)
}
