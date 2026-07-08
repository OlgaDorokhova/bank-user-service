package com.bank.controller;

import com.bank.dto.request.TransferRequest;
import com.bank.dto.response.TransferPageResponse;
import com.bank.dto.response.TransferResponse;
import com.bank.dto.response.TransferStatsResponse;
import com.bank.service.TransferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/transfers")
@RequiredArgsConstructor
@Tag(name = "Переводы", description = "Переводы между своими счетами")
@SecurityRequirement(name = "Bearer Authentication")
public class TransferController {

    private final TransferService transferService;

    @Operation(summary = "Перевести деньги", description = "Перевод между своими счетами. Сумма должна быть положительной, иначе ошибка")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Перевод выполнен успешно"),
            @ApiResponse(responseCode = "400", description = "Недостаточно средств или перевод самому себе"),
            @ApiResponse(responseCode = "401", description = "Не авторизован"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    })
    @PostMapping
    public ResponseEntity<TransferResponse> transferMoney(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody TransferRequest request) {

        Long fromUserId = Long.parseLong(userDetails.getUsername());
        log.info("Transfer request from user {} to user {}, amount: {}",
                fromUserId, request.getToUserId(), request.getAmount());

        TransferResponse response = transferService.transferMoney(fromUserId, request);
        return ResponseEntity.ok(response);
    }

    // ===== Эндпоинты для ADMIN =====

    @Operation(summary = "Получить все переводы пользователя с пагинацией (только ADMIN)")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/{userId}/all")
    public ResponseEntity<TransferPageResponse> getAllTransfers(
            @Parameter(description = "ID пользователя")
            @PathVariable Long userId,

            @Parameter(description = "Номер страницы (начиная с 0)")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Количество записей на странице")
            @RequestParam(defaultValue = "20") int size,

            @Parameter(description = "Поле для сортировки (createdAt, amount)")
            @RequestParam(defaultValue = "createdAt") String sortBy,

            @Parameter(description = "Направление сортировки (ASC, DESC)")
            @RequestParam(defaultValue = "DESC") String direction) {

        log.info("ADMIN: Getting all transfers for user {} (page={}, size={}, sortBy={}, direction={})",
                userId, page, size, sortBy, direction);

        return ResponseEntity.ok(transferService.getAllTransfersByUserId(userId, page, size, sortBy, direction));
    }

    @Operation(summary = "Получить исходящие переводы пользователя (только ADMIN)")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/{userId}/outgoing")
    public ResponseEntity<TransferPageResponse> getOutgoingTransfers(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.info("ADMIN: Getting outgoing transfers for user {}", userId);
        return ResponseEntity.ok(transferService.getOutgoingTransfers(userId, page, size));
    }

    @Operation(summary = "Получить входящие переводы пользователя (только ADMIN)")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/{userId}/incoming")
    public ResponseEntity<TransferPageResponse> getIncomingTransfers(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.info("ADMIN: Getting incoming transfers for user {}", userId);
        return ResponseEntity.ok(transferService.getIncomingTransfers(userId, page, size));
    }

    @Operation(summary = "Получить статистику по переводам пользователя (только ADMIN)")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/{userId}/stats")
    public ResponseEntity<TransferStatsResponse> getTransferStats(@PathVariable Long userId) {
        log.info("ADMIN: Getting transfer stats for user {}", userId);
        return ResponseEntity.ok(transferService.getTransferStats(userId));
    }
}