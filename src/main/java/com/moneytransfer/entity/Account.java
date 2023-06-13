package com.moneytransfer.entity;

import com.moneytransfer.enums.Currency;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "accounts")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class Account {
    @Version //optimistic
    protected int version;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id")
    private Long id;
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
    // TODO: Currency Exchange Mechanism (internal storage or API)
}
