package com.bank.service;

import com.bank.entity.Account;
import com.bank.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@EnableScheduling
@RequiredArgsConstructor
public class InterestSchedulerService {

    private final AccountRepository accountRepository;

    @Scheduled(fixedDelay = 30000)
    @Transactional
    public void applyInterest() {
        log.debug("Starting interest calculation for eligible accounts");

        List<Account> accounts = accountRepository.findAccountsEligibleForInterest();

        int updatedCount = 0;
        for (Account account : accounts) {
            BigDecimal currentBalance = account.getBalance();
            BigDecimal maxBalance = account.getInitialBalance().multiply(new BigDecimal("2.07"));

            BigDecimal interest = currentBalance.multiply(new BigDecimal("0.10"));
            BigDecimal newBalance = currentBalance.add(interest);

            if (newBalance.compareTo(maxBalance) > 0) {
                newBalance = maxBalance;
            }

            if (newBalance.compareTo(currentBalance) == 0) {
                continue;
            }

            account.setBalance(newBalance.setScale(2, RoundingMode.HALF_UP));
            account.setLastInterestDate(LocalDateTime.now());
            accountRepository.save(account);
            updatedCount++;

            log.debug("Applied interest to account {}: {} -> {}",
                    account.getUser().getId(), currentBalance, newBalance);
        }

        if (updatedCount > 0) {
            log.info("Interest applied to {} accounts", updatedCount);
        }
    }
}
