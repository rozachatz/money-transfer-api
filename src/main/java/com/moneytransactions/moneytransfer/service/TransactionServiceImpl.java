package com.moneytransactions.moneytransfer.service;

import com.moneytransactions.moneytransfer.dto.AccountsDTO;
import com.moneytransactions.moneytransfer.dto.TransferDTO;
import com.moneytransactions.moneytransfer.entity.Account;
import com.moneytransactions.moneytransfer.entity.Transaction;
import com.moneytransactions.moneytransfer.exceptions.AccountNotFoundException;
import com.moneytransactions.moneytransfer.exceptions.InsufficientBalanceException;
import com.moneytransactions.moneytransfer.exceptions.MoneyTransferException;
import com.moneytransactions.moneytransfer.exceptions.SameAccountException;
import com.moneytransactions.moneytransfer.repository.AccountRepository;
import com.moneytransactions.moneytransfer.repository.TransactionRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TransactionServiceImpl implements TransactionService { //responsible for business logic, error handling

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public TransactionServiceImpl(AccountRepository accountRepository, TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public TransferDTO transferFunds(Long sourceAccountId, Long targetAccountId, BigDecimal amount, String mode) throws MoneyTransferException {
        AccountsDTO accountsDTO = getAccountsByIds(sourceAccountId, targetAccountId, mode);

        validateTransfer(accountsDTO, sourceAccountId, targetAccountId, amount);

        Account sourceAccount = accountsDTO.getSourceAccount(), targetAccount = accountsDTO.getTargetAccount();

        sourceAccount.debit(amount);
        targetAccount.credit(amount);

        accountRepository.saveAll(List.of(sourceAccount, targetAccount));

        Transaction transaction = new Transaction(UUID.randomUUID(), sourceAccount, targetAccount, amount, "EUR");
        transactionRepository.save(transaction);
        return new TransferDTO(transaction.getId(), sourceAccountId, targetAccountId, amount, LocalDateTime.now(), "Money transferred successfully.");

    }

    public AccountsDTO getAccountsByIds(Long sourceAccountId, Long targetAccountId, String mode) throws AccountNotFoundException {
        Optional<AccountsDTO> accountsDTO;
        if (mode.equals("Pessimistic")) {
            accountsDTO = accountRepository.findByIdAndLockPessimistic(sourceAccountId, targetAccountId);
        } else {
            accountsDTO = accountRepository.findByIdAndLockOptimistic(sourceAccountId, targetAccountId);
        }
        return accountsDTO
                .stream()
                .findAny()
                .orElseThrow(() -> new AccountNotFoundException("Source/target account not found"));
    }

    public void validateTransfer(AccountsDTO accounts, Long sourceAccountId, Long targetAccountId, BigDecimal amount) throws MoneyTransferException {

        if (sourceAccountId.equals(targetAccountId)) {  /* AC3: Same Account */
            throw new SameAccountException("Transactions in the same account are not allowed.");
        }
        if (accounts.getSourceAccount().getBalance().compareTo(amount) < 0) {   /* AC2: Insufficient Balance */
            throw new InsufficientBalanceException("Insufficient balance in the source account.");
        }

    }

}