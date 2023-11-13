package com.moneytransfer.entity;

import com.moneytransfer.enums.Currency;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

/**
 * Entity that represents successful money transfers between two {@link Account}
 */
@Entity
@Table(name = "transactions")
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class Transaction {
    @Id
    private UUID id;

    @ManyToOne()
    @JoinColumn(name = "source_account_id", referencedColumnName = "id")
    private Account sourceAccount;

    @ManyToOne()
    @JoinColumn(name = "target_account_id", referencedColumnName = "id")
    private Account targetAccount;

    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private Currency currency;

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Transaction other = (Transaction) obj;
        return Objects.equals(id, other.id);
    }
}
