package com.moneytransfer.repository;

import com.moneytransfer.entity.Account;
import com.moneytransfer.enums.Currency;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ActiveProfiles("test")
@SpringBootTest
public class AccountRepositoryTest {

    @Autowired
    private AccountRepository accountRepository;

    @Test
    public void testCreateAndPersistAccountInH2() {
        UUID accountId = UUID.randomUUID();
        Account account = new Account(0, accountId, new BigDecimal("1000.00"), Currency.EUR, LocalDateTime.now());
        Account savedAccount = accountRepository.save(account);
        Optional<Account> retrievedAccount = accountRepository.findById(accountId);
        assertNotNull(savedAccount);
        Assertions.assertTrue(retrievedAccount.isPresent());
        Account actualAccount = retrievedAccount.get();
        assertEquals(accountId, actualAccount.getId());
        assertEquals(Currency.EUR, actualAccount.getCurrency());
        assertEquals(new BigDecimal("1000.00"), actualAccount.getBalance().setScale(2));
    }
}
