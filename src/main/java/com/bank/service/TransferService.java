package com.bank.service;

import com.bank.dto.request.TransferRequest;
import com.bank.dto.response.TransferResponse;
import com.bank.entity.Account;
import com.bank.entity.User;
import com.bank.exception.CustomExceptions;
import com.bank.repository.AccountRepository;
import com.bank.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransferService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    @Transactional
    public TransferResponse transferMoney(Long fromUserId, TransferRequest request) {

        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new CustomExceptions.InsufficientFundsException("Transfer amount must be positive");
        }
        if (fromUserId.equals(request.getToUserId())) {
            throw new CustomExceptions.SameUserTransferException("Cannot transfer money to yourself");
        }

        User toUser = userRepository.findById(request.getToUserId())
                .orElseThrow(() -> new CustomExceptions.UserNotFoundException("Recipient not found with id: " + request.getToUserId()));

        Account fromAccount = accountRepository.findByUserIdWithLock(fromUserId)
                .orElseThrow(() -> new CustomExceptions.UserNotFoundException("Sender account not found for user: " + fromUserId));

        Account toAccount = accountRepository.findByUserIdWithLock(request.getToUserId())
                .orElseThrow(() -> new CustomExceptions.UserNotFoundException("Recipient account not found for user: " + request.getToUserId()));

        if (fromAccount.getBalance().compareTo(request.getAmount()) < 0) {
            throw new CustomExceptions.InsufficientFundsException(
                    String.format("Insufficient funds. Balance: %.2f, Transfer amount: %.2f",
                            fromAccount.getBalance(), request.getAmount())
            );
        }

        BigDecimal newFromBalance = fromAccount.getBalance().subtract(request.getAmount());
        BigDecimal newToBalance = toAccount.getBalance().add(request.getAmount());

        fromAccount.setBalance(newFromBalance);
        toAccount.setBalance(newToBalance);

        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        log.info("Transfer {} from user {} to user {} completed successfully",
                request.getAmount(), fromUserId, request.getToUserId());

        return TransferResponse.builder()
                .fromUserId(fromUserId)
                .toUserId(request.getToUserId())
                .amount(request.getAmount())
                .status("COMPLETED")
                .timestamp(LocalDateTime.now())
                .build();
    }
}