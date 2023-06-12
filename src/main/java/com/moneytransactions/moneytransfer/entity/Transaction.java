package com.moneytransactions.moneytransfer.entity;

import com.moneytransactions.moneytransfer.enums.Currency;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "transactions")
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "transaction_id")
    private UUID id;

    @ManyToOne()
    @JoinColumn(name = "source_account_id", referencedColumnName = "account_id")
    private Account sourceAccount;
    @ManyToOne()
    @JoinColumn(name = "target_account_id", referencedColumnName = "account_id")
    private Account targetAccount;


    private BigDecimal amount;
    @Enumerated(EnumType.STRING)
    private Currency currency;



}
