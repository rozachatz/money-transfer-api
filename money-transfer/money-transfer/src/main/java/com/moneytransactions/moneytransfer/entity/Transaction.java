package com.moneytransactions.moneytransfer.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.lang.Long;

@Entity
@Table(name = "transactions")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // database should generate the ID value automatically.
    @Column(name = "transaction_id")
    private Long id;   // PRIMARY KEY
    private BigDecimal amount; //transaction amount
    private String currency; /*TO DO: enum?*/

    // Foreign keys
    @Column(name = "source_account_id")
    private Long sourceAccountId;

    @Column(name = "target_account_id")
    private Long targetAccountId;

    public Transaction(Long sourceAccountId, Long targetAccountId, BigDecimal amount, String currency) {
        this.sourceAccountId = sourceAccountId;
        this.targetAccountId = targetAccountId;
        this.amount = amount;
        this.currency = currency;
    }
    public Transaction() {

    }
    public BigDecimal getAmount() {
        return amount;
    }
    public Long getSourceAccountId() {
        return sourceAccountId;
    }
    public Long getTargetAccountId() {
        return targetAccountId;
    }
}
