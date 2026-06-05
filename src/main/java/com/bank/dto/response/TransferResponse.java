package com.bank.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class TransferResponse {
    private Long transferId;
    private Long fromUserId;
    private Long toUserId;
    private BigDecimal amount;
    private String status;
    private LocalDateTime timestamp;
}
