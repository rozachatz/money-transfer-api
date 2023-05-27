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
    private Long Id; // database should generate the ID value automatically.
    private BigDecimal balance;
    private String currency;
    private LocalDateTime createdAt;
    public Account(BigDecimal balance, String currency){
        this.balance = balance;
        this.currency = currency;
        this.createdAt = LocalDateTime.now();
    }
    public Account(){

    }
    public Long getId() {
        return Id;
    }
    public void setId(Long id) {
        Id = id;
    }
    public BigDecimal getBalance() {
        return balance;
    }

    // debit, credit methods
    public void credit(BigDecimal amount) {
        this.balance = balance.add(amount);
    }
    public void debit(BigDecimal amount) {
        this.balance=balance.subtract(amount);
    }
}
