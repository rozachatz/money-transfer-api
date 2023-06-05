package com.moneytransactions.moneytransfer.repository;

import com.moneytransactions.moneytransfer.dto.AccountsDTO;
import com.moneytransactions.moneytransfer.entity.Account;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a1 as sourceAccount, a2 as targetAccount FROM Account a1, Account a2 WHERE a1.id = :sourceAccountId AND a2.id = :targetAccountId")
    Optional<AccountsDTO> findByIdAndLockPessimistic(Long sourceAccountId, Long targetAccountId);

    @Lock(LockModeType.OPTIMISTIC)
    @Query("SELECT a1 as sourceAccount, a2 as targetAccount FROM Account a1, Account a2 WHERE a1.id = :sourceAccountId AND a2.id = :targetAccountId")
    Optional<AccountsDTO> findByIdAndLockOptimistic(Long sourceAccountId, Long targetAccountId);

}
