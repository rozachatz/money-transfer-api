package com.moneytransfer.entity;

import com.moneytransfer.enums.RequestStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "transaction_requests")
@Getter
@Setter
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class TransactionRequest {
    @Id
    private UUID requestId;
    @OneToOne()
    @JoinColumn(name = "transaction_id", referencedColumnName = "id")
    private Transaction transaction;//null if requestStatus = FAIL
    private RequestStatus requestStatus;
    private String jsonBody;
    private String errorMessage;//null if requestStatus IN (SUCCESS,IN_PROGRESS)

    public TransactionRequest(UUID requestId, RequestStatus requestStatus) {
        this.requestId = requestId;
        this.requestStatus = requestStatus;
    }

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
