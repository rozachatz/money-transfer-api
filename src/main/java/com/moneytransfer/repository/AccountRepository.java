package com.moneytransfer.repository;

import com.moneytransfer.dto.TransferAccountsDto;
import com.moneytransfer.entity.Account;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, UUID> {
    String GET_ACCOUNTS_QUERY = "SELECT " +
            "a1 as sourceAccount, a2 as targetAccount " +
            "FROM " +
            "Account a1, Account a2 " +
            "WHERE a1.id = :sourceAccountId AND a2.id = :targetAccountId";

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(value = GET_ACCOUNTS_QUERY)
    Optional<TransferAccountsDto> findByIdAndLockPessimistic(UUID sourceAccountId, UUID targetAccountId);

    @Lock(LockModeType.OPTIMISTIC)
    @Query(value = GET_ACCOUNTS_QUERY)
    Optional<TransferAccountsDto> findByIdAndLockOptimistic(UUID sourceAccountId, UUID targetAccountId);

    @Query(value = GET_ACCOUNTS_QUERY)
    Optional<TransferAccountsDto> findByIdAndLock(UUID sourceAccountId, UUID targetAccountId);

}
