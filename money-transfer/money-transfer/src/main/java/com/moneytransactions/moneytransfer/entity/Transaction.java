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

    // FOREIGN KEYS
    @ManyToOne()
    @JoinColumn(name="source_account_id", referencedColumnName = "account_id")
    private Account targetAccount;
    @ManyToOne()
    @JoinColumn(name="target_account_id", referencedColumnName = "account_id")
    private Account sourceAccount;
    public Transaction(Account sourceAccount, Account targetAccount, BigDecimal amount, String currency) {
        this.sourceAccount = sourceAccount;
        this.targetAccount = targetAccount;
        this.amount = amount;
        this.currency = currency;
    }

}
