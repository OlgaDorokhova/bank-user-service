package com.bank.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class JwtResponse {
    private String token;
    private String type = "Bearer";
    private Long userId;

    public JwtResponse(String token, Long userId) {
        this.token = token;
        this.userId = userId;
    }
}