package com.bank.repository;

import com.bank.entity.Transfer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TransferRepository extends JpaRepository<Transfer, Long> {

    // Получить все переводы пользователя (где он отправитель ИЛИ получатель)
    @Query("SELECT t FROM Transfer t " +
            "WHERE t.fromAccount.user.id = :userId " +
            "OR t.toAccount.user.id = :userId")
    Page<Transfer> findAllByUserId(@Param("userId") Long userId, Pageable pageable);

    // Получить переводы, где пользователь только отправитель
    @Query("SELECT t FROM Transfer t WHERE t.fromAccount.user.id = :userId")
    Page<Transfer> findOutgoingTransfers(@Param("userId") Long userId, Pageable pageable);

    // Получить переводы, где пользователь только получатель
    @Query("SELECT t FROM Transfer t WHERE t.toAccount.user.id = :userId")
    Page<Transfer> findIncomingTransfers(@Param("userId") Long userId, Pageable pageable);
}
