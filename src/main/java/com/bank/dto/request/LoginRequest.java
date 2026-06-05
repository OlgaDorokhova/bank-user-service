package com.bank.dto.request;

import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
public class LoginRequest {
    @NotBlank(message = "Email or phone is required")
    private String login;

    @NotBlank(message = "Password is required")
    private String password;
}
