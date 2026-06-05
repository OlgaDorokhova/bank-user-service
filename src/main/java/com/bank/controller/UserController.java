package com.bank.controller;

import com.bank.dto.request.UpdateUserRequest;
import com.bank.dto.request.UserSearchRequest;
import com.bank.dto.response.UserResponse;
import com.bank.dto.response.UserSearchResponse;
import com.bank.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/search")
    public ResponseEntity<UserSearchResponse> searchUsers(@Valid UserSearchRequest request) {
        log.info("Searching users with filters: name={}, email={}, phone={}, dateOfBirth={}",
                request.getName(), request.getEmail(), request.getPhone(), request.getDateOfBirth());

        Page<UserResponse> page = userService.searchUsers(
                request.getName(),
                request.getEmail(),
                request.getPhone(),
                request.getDateOfBirth(),
                request.getPage(),
                request.getSize()
        );

        UserSearchResponse response = UserSearchResponse.builder()
                .content(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        log.info("Getting current user info for ID: {}", userId);
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateCurrentUser(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdateUserRequest request) {
        Long userId = Long.parseLong(userDetails.getUsername());
        log.info("Updating user data for ID: {}", userId);
        return ResponseEntity.ok(userService.updateUserData(userId, request));
    }
}