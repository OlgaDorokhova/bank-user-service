package com.bank.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportHistoryResponse {

    private Long id;
    private String filename;
    private String format;
    private int totalRecords;
    private int successfulRecords;
    private int failedRecords;
    private String status;
    private String errorMessage;
    private Long importedBy;
    private LocalDateTime createdAt;
}
