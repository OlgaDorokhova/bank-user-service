package com.bank.service;

import com.bank.dto.request.ImportUserDto;
import com.bank.entity.Account;
import com.bank.entity.EmailData;
import com.bank.entity.PhoneData;
import com.bank.entity.User;
import com.bank.exception.ImportException;
import com.bank.repository.AccountRepository;
import com.bank.repository.EmailDataRepository;
import com.bank.repository.PhoneDataRepository;
import com.bank.repository.UserRepository;
import com.bank.service.imports.ImportParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final PasswordEncoder passwordEncoder;

    private static final String DEFAULT_PASSWORD = "password123";

    @Transactional
    public int importUsers(MultipartFile file, String format) {
        // 1. Выбираем парсер по формату
        ImportParser parser = importParsers.get(format.toLowerCase());
        if (parser == null) {
            throw new ImportException("Unsupported import format: " + format + ". Supported: csv, xlsx");
        }

        // 2. Парсим файл
        try {
            List<ImportUserDto> importedUsers = parser.parse(file.getInputStream());
            log.info("Parsed {} users from file", importedUsers.size());

            // 3. Сохраняем каждого пользователя
            int savedCount = 0;
            for (ImportUserDto dto : importedUsers) {
                if (saveUser(dto)) {
                    savedCount++;
                }
            }

            log.info("Successfully imported {} users", savedCount);
            return savedCount;

        } catch (ImportException e) {
            throw e;  // Пробрасываем дальше
        } catch (Exception e) {
            log.error("Import failed", e);
            throw new ImportException("Import failed: " + e.getMessage(), e);
        }
    }

    private boolean saveUser(ImportUserDto dto) {
        // Валидация обязательных полей
        if (dto.getName() == null || dto.getName().isEmpty()) {
            log.warn("User without name, skipping");
            return false;
        }

        if (dto.getEmail() == null || dto.getEmail().isEmpty()) {
            log.warn("User {} without email, skipping", dto.getName());
            return false;
        }

        if (dto.getPhone() == null || dto.getPhone().isEmpty()) {
            log.warn("User {} without phone, skipping", dto.getName());
            return false;
        }

        // Проверяем уникальность
        if (emailDataRepository.existsByEmail(dto.getEmail())) {
            log.warn("Email {} already exists, skipping user {}", dto.getEmail(), dto.getName());
            return false;
        }

        if (phoneDataRepository.existsByPhone(dto.getPhone())) {
            log.warn("Phone {} already exists, skipping user {}", dto.getPhone(), dto.getName());
            return false;
        }

        // Создаём пользователя
        User user = User.builder()
                .name(dto.getName())
                .dateOfBirth(dto.getDateOfBirth())
                .password(passwordEncoder.encode(DEFAULT_PASSWORD))
                .build();

        userRepository.save(user);

        // Добавляем email
        EmailData email = EmailData.builder()
                .email(dto.getEmail())
                .user(user)
                .build();
        user.addEmail(email);

        // Добавляем телефон
        PhoneData phone = PhoneData.builder()
                .phone(dto.getPhone())
                .user(user)
                .build();
        user.addPhone(phone);

        // Добавляем счёт
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