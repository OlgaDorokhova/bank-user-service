package com.bank.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserReportDto {
    private Long userId;
    private String name;
    private LocalDate dateOfBirth;
    private String emails;      // объединённые через ";"
    private String phones;      // объединённые через ";"
    private BigDecimal balance;
    private BigDecimal initialBalance;
}
