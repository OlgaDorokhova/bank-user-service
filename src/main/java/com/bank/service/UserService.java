package com.bank.service;

import com.bank.dto.request.UpdateUserRequest;
import com.bank.dto.response.UserResponse;
import com.bank.entity.EmailData;
import com.bank.entity.PhoneData;
import com.bank.entity.User;
import com.bank.enums.Role;
import com.bank.exception.CustomExceptions;
import com.bank.mapper.UserMapper;
import com.bank.repository.EmailDataRepository;
import com.bank.repository.PhoneDataRepository;
import com.bank.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final EmailDataRepository emailDataRepository;
    private final PhoneDataRepository phoneDataRepository;
    private final UserMapper userMapper;
    private final CacheService cacheService;

    @Transactional(readOnly = true)
    public Page<UserResponse> searchUsers(String name, String email, String phone, LocalDate dateOfBirth,
                                          Integer page, Integer size) {
        String dateOfBirthStr = dateOfBirth != null ? dateOfBirth.toString() : null;
        List<UserResponse> cachedResult = cacheService.cacheUserSearch(
                name, email, phone, dateOfBirthStr, page, size);

        if (cachedResult != null && !cachedResult.isEmpty()) {
            log.debug("Returning cached search results");
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<User> users = userRepository.searchUsers(name, email, phone, dateOfBirth, pageable);
        Page<UserResponse> result = users.map(userMapper::toResponse);

        if (result.hasContent()) {
            cacheService.cacheUserSearch(name, email, phone, dateOfBirthStr, page, size);
        }

        return result;
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomExceptions.UserNotFoundException("User not found with id: " + userId));
        return userMapper.toResponse(user);
    }

    @Transactional
    public UserResponse updateUserData(Long userId, UpdateUserRequest request) {
        cacheService.evictUserSearchCache();
        cacheService.evictUserByIdCache(userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomExceptions.UserNotFoundException("User not found with id: " + userId));

        if (request.getEmails() != null) {
            updateEmails(user, request.getEmails());
        }

        if (request.getPhones() != null) {
            updatePhones(user, request.getPhones());
        }

        User savedUser = userRepository.save(user);
        log.info("User {} updated their contact data", userId);
        return userMapper.toResponse(savedUser);
    }

    private void updateEmails(User user, List<UpdateUserRequest.EmailOperation> operations) {
        for (UpdateUserRequest.EmailOperation op : operations) {
            switch (op.getOperation().toUpperCase()) {
                case "ADD":
                    if (emailDataRepository.existsByEmail(op.getNewEmail())) {
                        throw new CustomExceptions.EmailAlreadyExistsException("Email already exists: " + op.getNewEmail());
                    }
                    EmailData newEmail = EmailData.builder()
                            .email(op.getNewEmail())
                            .user(user)
                            .build();
                    user.addEmail(newEmail);
                    log.debug("Added email {} to user {}", op.getNewEmail(), user.getId());
                    break;

                case "DELETE":
                    EmailData emailToDelete = user.getEmails().stream()
                            .filter(e -> e.getEmail().equals(op.getOldEmail()))
                            .findFirst()
                            .orElseThrow(() -> new CustomExceptions.EmailNotFoundException("Email not found: " + op.getOldEmail()));
                    user.removeEmail(emailToDelete);
                    emailDataRepository.delete(emailToDelete);
                    log.debug("Deleted email {} from user {}", op.getOldEmail(), user.getId());
                    break;

                case "UPDATE":
                    EmailData emailToUpdate = user.getEmails().stream()
                            .filter(e -> e.getEmail().equals(op.getOldEmail()))
                            .findFirst()
                            .orElseThrow(() -> new CustomExceptions.EmailNotFoundException("Email not found: " + op.getOldEmail()));

                    if (emailDataRepository.existsByEmail(op.getNewEmail())) {
                        throw new CustomExceptions.EmailAlreadyExistsException("Email already exists: " + op.getNewEmail());
                    }
                    emailToUpdate.setEmail(op.getNewEmail());
                    log.debug("Updated email from {} to {} for user {}", op.getOldEmail(), op.getNewEmail(), user.getId());
                    break;

                default:
                    log.warn("Unknown email operation: {}", op.getOperation());
            }
        }
    }

    private void updatePhones(User user, List<UpdateUserRequest.PhoneOperation> operations) {
        for (UpdateUserRequest.PhoneOperation op : operations) {
            switch (op.getOperation().toUpperCase()) {
                case "ADD":
                    if (phoneDataRepository.existsByPhone(op.getNewPhone())) {
                        throw new CustomExceptions.PhoneAlreadyExistsException("Phone already exists: " + op.getNewPhone());
                    }
                    PhoneData newPhone = PhoneData.builder()
                            .phone(op.getNewPhone())
                            .user(user)
                            .build();
                    user.addPhone(newPhone);
                    log.debug("Added phone {} to user {}", op.getNewPhone(), user.getId());
                    break;

                case "DELETE":
                    PhoneData phoneToDelete = user.getPhones().stream()
                            .filter(p -> p.getPhone().equals(op.getOldPhone()))
                            .findFirst()
                            .orElseThrow(() -> new CustomExceptions.PhoneNotFoundException("Phone not found: " + op.getOldPhone()));
                    user.removePhone(phoneToDelete);
                    phoneDataRepository.delete(phoneToDelete);
                    log.debug("Deleted phone {} from user {}", op.getOldPhone(), user.getId());
                    break;

                case "UPDATE":
                    PhoneData phoneToUpdate = user.getPhones().stream()
                            .filter(p -> p.getPhone().equals(op.getOldPhone()))
                            .findFirst()
                            .orElseThrow(() -> new CustomExceptions.PhoneNotFoundException("Phone not found: " + op.getOldPhone()));

                    if (phoneDataRepository.existsByPhone(op.getNewPhone())) {
                        throw new CustomExceptions.PhoneAlreadyExistsException("Phone already exists: " + op.getNewPhone());
                    }
                    phoneToUpdate.setPhone(op.getNewPhone());
                    log.debug("Updated phone from {} to {} for user {}", op.getOldPhone(), op.getNewPhone(), user.getId());
                    break;

                default:
                    log.warn("Unknown phone operation: {}", op.getOperation());
            }
        }
    }

    @Transactional
    public UserResponse changeUserRole(Long userId, String roleName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomExceptions.UserNotFoundException("User not found: " + userId));

        try {
            Role newRole = Role.valueOf(roleName.toUpperCase());
            user.setRole(newRole);
            User saved = userRepository.save(user);
            log.info("Role changed for user {} to {}", userId, newRole);
            return userMapper.toResponse(saved);
        } catch (IllegalArgumentException e) {
            throw new CustomExceptions.ValidationException("Invalid role: " + roleName + ". Allowed: ADMIN, USER");
        }
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void blockUser(Long userId) {
        // TODO: добавить поле isBlocked в User
        log.info("Blocking user {}", userId);
        // userRepository.updateBlockedStatus(userId, true);
    }

    @Transactional
    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
        log.info("Deleted user {}", userId);
    }
}