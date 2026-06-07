package com.bank.controller;

import com.bank.config.JwtUtil;
import com.bank.dto.request.LoginRequest;
import com.bank.dto.response.JwtResponse;
import com.bank.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Аутентификация", description = "Логин и получение JWT токена")
public class AuthController {

    @GetMapping("/test")
    public String test() {
        return "OK";
    }

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @Operation(summary = "Авторизация пользователя", description = "Возвращает JWT токен для доступа к API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешная авторизация",
                    content = @Content(examples = @ExampleObject(value = "{\"token\":\"eyJhbGc...\",\"type\":\"Bearer\",\"userId\":1}"))),
            @ApiResponse(responseCode = "401", description = "Неверный логин или пароль")
    })
    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("1. Login attempt: {}", request.getLogin());
        log.info("2. Password: {}", request.getPassword());

        try {
            log.info("3. Before authenticationManager.authenticate");
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getLogin(), request.getPassword())
            );
            log.info("4. Authentication successful");

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            log.info("5. UserDetails username: {}", userDetails.getUsername());

            Long userId = Long.parseLong(userDetails.getUsername());
            log.info("6. Parsed userId: {}", userId);

            String token = jwtUtil.generateToken(userId);
            log.info("7. Token generated: {}", token.substring(0, 20) + "...");

            return ResponseEntity.ok(new JwtResponse(token, userId));

        } catch (Exception e) {
            log.error("Login error: ", e);  // ← полный стек трейс
            throw e;
        }
    }
}