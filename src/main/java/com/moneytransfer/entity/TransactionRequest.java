package com.moneytransfer.entity;

import com.moneytransfer.enums.RequestStatus;
import com.moneytransfer.service.TransactionRequestService;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;
import java.util.UUID;

/**
 * Entity that represents a request for a new {@link Transaction}
 */
@Entity
@Table(name = "transaction_requests")
@Getter
@Setter
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
@AllArgsConstructor
public class TransactionRequest {
    @Id
    @Column(name = "request_id")
    private UUID requestId;
    @OneToOne()
    @JoinColumn(name = "transaction_id", referencedColumnName = "id")
    private Transaction transaction;
    private RequestStatus requestStatus;
    private String jsonBody;
    private String errorMessage;

    @Override
    public int hashCode() {
        return Objects.hash(requestId);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        TransactionRequest other = (TransactionRequest) obj;
        return Objects.equals(requestId, other.requestId);
    }

}
