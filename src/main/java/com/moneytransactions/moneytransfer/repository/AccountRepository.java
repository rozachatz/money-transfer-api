package com.moneytransactions.moneytransfer.repository;

import com.moneytransactions.moneytransfer.entity.Account;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AccountRepository extends JpaRepository<Account, Long> {
    //responsible for Create, Read (SELECT), Update, Delete and @Query operations
    //provides access to the database
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Account a WHERE a.id IN :ids")
    List<Account> findAllByIdAndLock(@Param("ids") List<Long> ids);
}
