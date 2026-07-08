package com.bank.service;

import com.bank.dto.request.TransferRequest;
import com.bank.dto.response.TransferPageResponse;
import com.bank.dto.response.TransferResponse;
import com.bank.dto.response.TransferStatsResponse;
import com.bank.entity.Account;
import com.bank.entity.Transfer;
import com.bank.entity.User;
import com.bank.enums.TransferStatus;
import com.bank.exception.CustomExceptions;
import com.bank.mapper.TransferMapper;
import com.bank.repository.AccountRepository;
import com.bank.repository.TransferRepository;
import com.bank.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransferService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final TransferRepository transferRepository;
    private final TransferMapper transferMapper;

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

        // ===== СОЗДАЁМ И СОХРАНЯЕМ ПЕРЕВОД =====
        Transfer transfer = Transfer.builder()
                .fromAccount(fromAccount)
                .toAccount(toAccount)
                .amount(request.getAmount())
                .status(TransferStatus.COMPLETED)
                .createdAt(LocalDateTime.now())
                .build();

        transferRepository.save(transfer);  // ← сохраняем в БД

        log.info("Transfer {} from user {} to user {} completed",
                request.getAmount(), fromUserId, request.getToUserId());

        // Возвращаем DTO с данными из сохранённого перевода
        return transferMapper.toResponse(transfer);
    }

    /**
     * Получить все переводы пользователя с пагинацией (только для ADMIN)
     */
    @Transactional(readOnly = true)
    public TransferPageResponse getAllTransfersByUserId(Long userId, int page, int size, String sortBy, String direction) {
        // Проверяем, что пользователь существует
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomExceptions.UserNotFoundException("User not found: " + userId));

        // Настраиваем сортировку
        Sort.Direction sortDirection = Sort.Direction.fromString(
                direction != null ? direction : "DESC"
        );
        String sortField = sortBy != null ? sortBy : "createdAt";

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortField));

        // Получаем переводы с пагинацией
        Page<Transfer> transferPage = transferRepository.findAllByUserId(userId, pageable);

        // Конвертируем в DTO
        List<TransferResponse> content = transferPage.getContent().stream()
                .map(transferMapper::toResponse)
                .collect(Collectors.toList());

        log.info("Fetched {} transfers for user {} (page {}, size {})",
                content.size(), userId, page, size);

        return TransferPageResponse.builder()
                .content(content)
                .page(transferPage.getNumber())
                .size(transferPage.getSize())
                .totalElements(transferPage.getTotalElements())
                .totalPages(transferPage.getTotalPages())
                .last(transferPage.isLast())
                .userId(userId)
                .build();
    }

    /**
     * Получить исходящие переводы пользователя (только для ADMIN)
     */
    @Transactional(readOnly = true)
    public TransferPageResponse getOutgoingTransfers(Long userId, int page, int size) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomExceptions.UserNotFoundException("User not found: " + userId));

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Transfer> transferPage = transferRepository.findOutgoingTransfers(userId, pageable);

        List<TransferResponse> content = transferPage.getContent().stream()
                .map(transferMapper::toResponse)
                .collect(Collectors.toList());

        return TransferPageResponse.builder()
                .content(content)
                .page(transferPage.getNumber())
                .size(transferPage.getSize())
                .totalElements(transferPage.getTotalElements())
                .totalPages(transferPage.getTotalPages())
                .last(transferPage.isLast())
                .userId(userId)
                .build();
    }

    /**
     * Получить входящие переводы пользователя (только для ADMIN)
     */
    @Transactional(readOnly = true)
    public TransferPageResponse getIncomingTransfers(Long userId, int page, int size) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomExceptions.UserNotFoundException("User not found: " + userId));

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Transfer> transferPage = transferRepository.findIncomingTransfers(userId, pageable);

        List<TransferResponse> content = transferPage.getContent().stream()
                .map(transferMapper::toResponse)
                .collect(Collectors.toList());

        return TransferPageResponse.builder()
                .content(content)
                .page(transferPage.getNumber())
                .size(transferPage.getSize())
                .totalElements(transferPage.getTotalElements())
                .totalPages(transferPage.getTotalPages())
                .last(transferPage.isLast())
                .userId(userId)
                .build();
    }

    @Transactional(readOnly = true)
    public TransferStatsResponse getTransferStats(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomExceptions.UserNotFoundException("User not found: " + userId));

        Page<Transfer> allTransfers = transferRepository.findAllByUserId(userId, Pageable.unpaged());
        Page<Transfer> outgoing = transferRepository.findOutgoingTransfers(userId, Pageable.unpaged());
        Page<Transfer> incoming = transferRepository.findIncomingTransfers(userId, Pageable.unpaged());

        BigDecimal totalOutgoing = outgoing.getContent().stream()
                .map(Transfer::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalIncoming = incoming.getContent().stream()
                .map(Transfer::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long outgoingCount = outgoing.getTotalElements();
        long incomingCount = incoming.getTotalElements();

        return TransferStatsResponse.builder()
                .userId(userId)
                .totalTransfers(allTransfers.getTotalElements())
                .outgoingCount(outgoingCount)
                .incomingCount(incomingCount)
                .totalOutgoingAmount(totalOutgoing)
                .totalIncomingAmount(totalIncoming)
                .avgOutgoingAmount(outgoingCount > 0 ? totalOutgoing.divide(BigDecimal.valueOf(outgoingCount), 2) : BigDecimal.ZERO)
                .avgIncomingAmount(incomingCount > 0 ? totalIncoming.divide(BigDecimal.valueOf(incomingCount), 2) : BigDecimal.ZERO)
                .build();
    }
}