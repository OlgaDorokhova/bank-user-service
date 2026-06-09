package com.bank.dto.request;

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
public class ImportUserDto {
    private String name;
    private LocalDate dateOfBirth;
    private String email;
    private String phone;
    private BigDecimal initialBalance;
}
