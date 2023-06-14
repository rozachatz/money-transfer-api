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
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a1 as sourceAccount, a2 as targetAccount FROM Account a1, Account a2 WHERE a1.id = :sourceAccountId AND a2.id = :targetAccountId")
    Optional<TransferAccountsDto> findByIdAndLockPessimistic(UUID sourceAccountId, UUID targetAccountId);

    @Lock(LockModeType.OPTIMISTIC)
    @Query("SELECT a1 as sourceAccount, a2 as targetAccount FROM Account a1, Account a2 WHERE a1.id = :sourceAccountId AND a2.id = :targetAccountId")
    Optional<TransferAccountsDto> findByIdAndLockOptimistic(UUID sourceAccountId, UUID targetAccountId);

    @Query("SELECT a1 as sourceAccount, a2 as targetAccount FROM Account a1, Account a2 WHERE a1.id = :sourceAccountId AND a2.id = :targetAccountId")
    Optional<TransferAccountsDto> findByIdAndLock(UUID sourceAccountId, UUID targetAccountId);

}
