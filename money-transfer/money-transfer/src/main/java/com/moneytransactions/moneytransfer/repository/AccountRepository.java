package com.moneytransactions.moneytransfer.repository;
import com.moneytransactions.moneytransfer.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
public interface AccountRepository extends JpaRepository<Account,Long> {
   // responsible for database query operations (+CRUD: Create, Read, Update, Delete).
}
