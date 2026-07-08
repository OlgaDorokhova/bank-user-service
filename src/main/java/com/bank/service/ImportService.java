package com.bank.service;

import com.bank.dto.request.ImportUserDto;
import com.bank.dto.response.ImportHistoryResponse;
import com.bank.entity.Account;
import com.bank.entity.EmailData;
import com.bank.entity.ImportHistory;
import com.bank.entity.PhoneData;
import com.bank.entity.User;
import com.bank.exception.CustomExceptions.ImportException;
import com.bank.mapper.ImportHistoryMapper;
import com.bank.repository.AccountRepository;
import com.bank.repository.EmailDataRepository;
import com.bank.repository.ImportHistoryRepository;
import com.bank.repository.PhoneDataRepository;
import com.bank.repository.UserRepository;
import com.bank.service.imports.ImportParser;
import com.bank.validation.ImportValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImportService {

    private final Map<String, ImportParser> importParsers;
    private final UserRepository userRepository;
    private final EmailDataRepository emailDataRepository;
    private final PhoneDataRepository phoneDataRepository;
    private final AccountRepository accountRepository;
    private final ImportHistoryRepository importHistoryRepository;
    private final PasswordEncoder passwordEncoder;
    private final ImportValidator importValidator;
    private final ImportHistoryMapper importHistoryMapper; // ← добавили

    private static final String DEFAULT_PASSWORD = "password123";

    @Transactional
    public int importUsers(MultipartFile file, String format) {
        ImportParser parser = importParsers.get(format.toLowerCase());
        if (parser == null) {
            throw new ImportException("Unsupported import format: " + format);
        }

        Long currentUserId = getCurrentUserId();

        try {
            List<ImportUserDto> importedUsers = parser.parse(file.getInputStream());
            log.info("Parsed {} users from file", importedUsers.size());

            int successfulCount = 0;
            int failedCount = 0;

            for (ImportUserDto dto : importedUsers) {
                if (saveUser(dto)) {
                    successfulCount++;
                } else {
                    failedCount++;
                }
            }

            // Сохраняем историю импорта
            saveImportHistory(file.getOriginalFilename(), format,
                    importedUsers.size(), successfulCount, failedCount,
                    ImportHistory.ImportStatus.SUCCESS, null, currentUserId);

            log.info("Successfully imported {} users ({} failed)", successfulCount, failedCount);
            return successfulCount;

        } catch (Exception e) {
            // Сохраняем историю с ошибкой
            saveImportHistory(file.getOriginalFilename(), format,
                    0, 0, 0,
                    ImportHistory.ImportStatus.FAILED,
                    e.getMessage(),
                    currentUserId);

            log.error("Import failed", e);
            throw new ImportException("Import failed: " + e.getMessage());
        }
    }

    private void saveImportHistory(String filename, String format,
                                   int total, int success, int failed,
                                   ImportHistory.ImportStatus status,
                                   String errorMessage, Long userId) {
        ImportHistory history = ImportHistory.builder()
                .filename(filename)
                .format(format)
                .totalRecords(total)
                .successfulRecords(success)
                .failedRecords(failed)
                .status(status)
                .errorMessage(errorMessage)
                .importedBy(userId)
                .build();

        importHistoryRepository.save(history);
        log.info("Import history saved for file: {}", filename);
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        String userId = authentication.getName();  // username = userId
        return Long.parseLong(userId);
    }

    @Transactional(readOnly = true)
    public Page<ImportHistoryResponse> getImportHistory(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ImportHistory> historyPage = importHistoryRepository.findAllByOrderByCreatedAtDesc(pageable);
        return historyPage.map(importHistoryMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<ImportHistoryResponse> getImportHistoryByUser(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ImportHistory> historyPage = importHistoryRepository.findByImportedBy(userId, pageable);
        return historyPage.map(importHistoryMapper::toResponse);
    }


    private boolean saveUser(ImportUserDto dto) {
        // 1. Валидация через ImportValidator
        ImportValidator.ValidationResult validation = importValidator.validate(dto);

        if (!validation.isValid()) {
            log.warn("Validation failed: {}", validation.getErrorMessage());
            return false;
        }

        // 2. Проверка уникальности
        if (emailDataRepository.existsByEmail(dto.getEmail())) {
            log.warn("Email {} already exists, skipping", dto.getEmail());
            return false;
        }

        if (phoneDataRepository.existsByPhone(dto.getPhone())) {
            log.warn("Phone {} already exists, skipping", dto.getPhone());
            return false;
        }

        // 3. Создаём пользователя
        User user = User.builder()
                .name(dto.getName())
                .dateOfBirth(dto.getDateOfBirth())
                .password(passwordEncoder.encode(DEFAULT_PASSWORD))
                .build();

        userRepository.save(user);

        // 4. Email
        EmailData email = EmailData.builder()
                .email(dto.getEmail())
                .user(user)
                .build();
        user.addEmail(email);

        // 5. Телефон
        PhoneData phone = PhoneData.builder()
                .phone(dto.getPhone())
                .user(user)
                .build();
        user.addPhone(phone);

        // 6. Счёт
        BigDecimal initialBalance = dto.getInitialBalance() != null ? dto.getInitialBalance() : BigDecimal.ZERO;
        Account account = Account.builder()
                .balance(initialBalance)
                .initialBalance(initialBalance)
                .user(user)
                .build();
        user.setAccount(account);

        userRepository.save(user);
        log.debug("Imported user: {}", dto.getEmail());

        return true;
    }
}