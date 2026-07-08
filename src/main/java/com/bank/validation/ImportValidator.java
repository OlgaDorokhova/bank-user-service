package com.bank.validation;

import com.bank.dto.request.ImportUserDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.regex.Pattern;

@Slf4j
@Component
public class ImportValidator {

    // Регулярные выражения для валидации
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^79\\d{9}$");  // Российские номера: 79 + 9 цифр

    private static final Pattern NAME_PATTERN =
            Pattern.compile("^[А-Яа-яA-Za-z\\s-]{2,100}$");

    private static final BigDecimal MIN_BALANCE = BigDecimal.ZERO;
    private static final BigDecimal MAX_BALANCE = new BigDecimal("1000000000");

    public ValidationResult validate(ImportUserDto dto) {
        ValidationResult result = new ValidationResult();

        // 1. Проверка имени
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            result.addError("name", "Имя не может быть пустым");
        } else if (!NAME_PATTERN.matcher(dto.getName()).matches()) {
            result.addError("name", "Имя должно содержать только буквы, пробелы и дефис (2-100 символов)");
        }

        // 2. Проверка email
        if (dto.getEmail() == null || dto.getEmail().trim().isEmpty()) {
            result.addError("email", "Email не может быть пустым");
        } else if (!EMAIL_PATTERN.matcher(dto.getEmail()).matches()) {
            result.addError("email", "Неверный формат email: " + dto.getEmail());
        }

        // 3. Проверка телефона
        if (dto.getPhone() == null || dto.getPhone().trim().isEmpty()) {
            result.addError("phone", "Телефон не может быть пустым");
        } else if (!PHONE_PATTERN.matcher(dto.getPhone()).matches()) {
            result.addError("phone", "Неверный формат телефона. Ожидается: 79XXXXXXXXX (11 цифр)");
        }

        // 4. Проверка даты рождения
        if (dto.getDateOfBirth() == null) {
            result.addError("dateOfBirth", "Дата рождения не может быть пустой");
        } else if (dto.getDateOfBirth().isAfter(java.time.LocalDate.now())) {
            result.addError("dateOfBirth", "Дата рождения не может быть в будущем");
        } else if (dto.getDateOfBirth().isBefore(java.time.LocalDate.now().minusYears(120))) {
            result.addError("dateOfBirth", "Возраст не может быть больше 120 лет");
        }

        // 5. Проверка баланса
        if (dto.getInitialBalance() == null) {
            result.addError("initialBalance", "Начальный баланс не может быть пустым");
        } else if (dto.getInitialBalance().compareTo(MIN_BALANCE) < 0) {
            result.addError("initialBalance", "Баланс не может быть отрицательным");
        } else if (dto.getInitialBalance().compareTo(MAX_BALANCE) > 0) {
            result.addError("initialBalance", "Баланс не может превышать " + MAX_BALANCE);
        }

        return result;
    }

    // Класс для сбора ошибок валидации
    public static class ValidationResult {
        private final java.util.Map<String, String> errors = new java.util.LinkedHashMap<>();
        private boolean valid = true;

        public void addError(String field, String message) {
            errors.put(field, message);
            valid = false;
        }

        public boolean isValid() {
            return valid;
        }

        public java.util.Map<String, String> getErrors() {
            return errors;
        }

        public String getErrorMessage() {
            return errors.entrySet().stream()
                    .map(e -> e.getKey() + ": " + e.getValue())
                    .collect(java.util.stream.Collectors.joining("; "));
        }
    }
}