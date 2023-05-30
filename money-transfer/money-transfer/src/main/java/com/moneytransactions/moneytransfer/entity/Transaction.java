package com.moneytransactions.moneytransfer.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity //JPA entity: class mapped to a database table (accessed via JDBC driver)
@Table(name = "transactions")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID) // (atomically) generated in service (eliminate bottleneck)
    @Column(name = "transaction_id")
    private UUID id;   // PRIMARY KEY
    private BigDecimal amount; //transaction amount
    private String currency; /* TO DO: enum + currency exchange */

    /* FOREIGN KEYS (Many transactions -> One account) */
    @ManyToOne()
    @JoinColumn(name = "source_account_id", referencedColumnName = "account_id")
    private Account sourceAccount;
    @ManyToOne()
    @JoinColumn(name = "target_account_id", referencedColumnName = "account_id")
    private Account targetAccount;

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
