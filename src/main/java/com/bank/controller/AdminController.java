package com.bank.controller;

import com.bank.dto.response.UserResponse;
import com.bank.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Администратор", description = "Управление пользователями (только для ADMIN)")
@SecurityRequirement(name = "Bearer Authentication")
public class AdminController {

    private final UserService userService;

    @Operation(summary = "Получить всех пользователей (только ADMIN)")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        log.info("ADMIN: Getting all users");
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @Operation(summary = "Изменить роль пользователя (только ADMIN)")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/users/{userId}/role")
    public ResponseEntity<UserResponse> changeUserRole(
            @PathVariable Long userId,
            @RequestParam String role) {
        log.info("ADMIN: Changing role for user {} to {}", userId, role);
        return ResponseEntity.ok(userService.changeUserRole(userId, role));
    }

    @Operation(summary = "Заблокировать пользователя (только ADMIN)")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/users/{userId}/block")
    public ResponseEntity<Void> blockUser(@PathVariable Long userId) {
        log.info("ADMIN: Blocking user {}", userId);
        userService.blockUser(userId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Удалить пользователя (только ADMIN)")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        log.info("ADMIN: Deleting user {}", userId);
        userService.deleteUser(userId);
        return ResponseEntity.ok().build();
    }
}
