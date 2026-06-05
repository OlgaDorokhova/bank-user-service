package com.bank.repository;

import com.bank.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByUserId(Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Account a WHERE a.user.id = :userId")
    Optional<Account> findByUserIdWithLock(@Param("userId") Long userId);

    @Query("SELECT a FROM Account a WHERE a.balance < a.initialBalance * 2.07")
    List<Account> findAccountsEligibleForInterest();

    @Query("UPDATE Account a SET a.balance = a.balance + :amount WHERE a.user.id = :userId")
    void addBalance(@Param("userId") Long userId, @Param("amount") BigDecimal amount);
}