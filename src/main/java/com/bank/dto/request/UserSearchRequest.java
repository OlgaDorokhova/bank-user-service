package com.bank.dto.request;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import javax.validation.constraints.Min;
import java.time.LocalDate;

@Data
public class UserSearchRequest {
    private String name;
    private String phone;
    private String email;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateOfBirth;

    @Min(0)
    private Integer page = 0;

    @Min(1)
    private Integer size = 10;
}
