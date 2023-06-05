package com.moneytransactions.moneytransfer.entity;

import jakarta.persistence.*;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity //JPA entity: class mapped to a database table (accessed via JDBC driver)
@Table(name = "transactions")
@Getter
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID) // (atomically) generated in service (eliminate bottleneck)
    @Column(name = "transaction_id")
    private UUID id; //PRIMARY KEY
    private BigDecimal amount; //amount to be transferred
    private String currency;

    @ManyToOne()
    @JoinColumn(name = "source_account_id", referencedColumnName = "account_id")
    private Account sourceAccount;
    @ManyToOne()
    @JoinColumn(name = "target_account_id", referencedColumnName = "account_id")
    private Account targetAccount;

    /*
     * CONSTRUCTORS
     */
    public Transaction(UUID id, Account sourceAccount, Account targetAccount, BigDecimal amount, String currency) {
        this.id = id;
        this.sourceAccount = sourceAccount;
        this.targetAccount = targetAccount;
        this.amount = amount;
        this.currency = currency;
    }

    private Transaction() {
    }

}
