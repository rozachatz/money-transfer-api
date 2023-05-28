package com.moneytransactions.moneytransfer.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.lang.Long;

@Entity //JPA entity: class mapped to a database table (accessed via JDBC driver)
@Table(name = "transactions")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // database should generate the ID value automatically.
    @Column(name = "transaction_id")
    private Long id;   // PRIMARY KEY
    private BigDecimal amount; //transaction amount
    private String currency; /* TO DO: enum + currency exchange */

    /* FOREIGN KEYS (Many transactions -> One account) */
    @ManyToOne()
    @JoinColumn(name="source_account_id", referencedColumnName = "account_id")
    private Account sourceAccount;
    @ManyToOne()
    @JoinColumn(name="target_account_id", referencedColumnName = "account_id")
    private Account targetAccount;

    public Transaction(Account sourceAccount, Account targetAccount, BigDecimal amount, String currency) {
        this.sourceAccount = sourceAccount;
        this.targetAccount = targetAccount;
        this.amount = amount;
        this.currency = currency;
    }
    private Transaction(){

    }
}
