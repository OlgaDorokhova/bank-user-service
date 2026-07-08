package com.bank.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferStatsResponse {
    private Long userId;
    private long totalTransfers;
    private long outgoingCount;
    private long incomingCount;
    private BigDecimal totalOutgoingAmount;
    private BigDecimal totalIncomingAmount;
    private BigDecimal avgOutgoingAmount;
    private BigDecimal avgIncomingAmount;
}
