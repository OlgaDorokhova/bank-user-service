package com.bank.controller;

import com.bank.dto.request.TransferRequest;
import com.bank.dto.response.TransferResponse;
import com.bank.service.TransferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

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
}