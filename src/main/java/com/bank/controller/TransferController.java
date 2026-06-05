package com.bank.controller;

import com.bank.dto.request.TransferRequest;
import com.bank.dto.response.TransferResponse;
import com.bank.service.TransferService;
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
public class TransferController {

    private final TransferService transferService;

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