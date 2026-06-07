package com.bank.dev;

import com.bank.entity.Account;
import com.bank.entity.EmailData;
import com.bank.entity.PhoneData;
import com.bank.entity.User;
import com.github.javafaker.Faker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataGenerator {

    private final PasswordEncoder passwordEncoder;
    private final Faker faker = new Faker(new Locale("ru"));

    private static final String DEFAULT_PASSWORD = "password123";

    public List<User> generateUsers(int count) {
        List<User> users = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            User user = generateSingleUser(i + 1);
            users.add(user);
        }

        log.info("Generated {} test users", users.size());
        return users;
    }

    private User generateSingleUser(int index) {
        // Основные данные пользователя
        User user = User.builder()
                .name(faker.name().fullName())
                .dateOfBirth(generateRandomBirthDate())
                .password(passwordEncoder.encode(DEFAULT_PASSWORD))
                .build();

        // Email (1-3 штуки)
        int emailCount = faker.random().nextInt(1, 3);
        for (int i = 0; i < emailCount; i++) {
            EmailData email = EmailData.builder()
                    .email(generateUniqueEmail(user.getName(), i))
                    .user(user)
                    .build();
            user.addEmail(email);
        }

        // Телефон (1-2 штуки)
        int phoneCount = faker.random().nextInt(1, 2);
        for (int i = 0; i < phoneCount; i++) {
            PhoneData phone = PhoneData.builder()
                    .phone(generatePhone())
                    .user(user)
                    .build();
            user.addPhone(phone);
        }

        // Счёт
        BigDecimal initialBalance = generateRandomBalance();
        Account account = Account.builder()
                .balance(initialBalance)
                .initialBalance(initialBalance)
                .user(user)
                .build();
        user.setAccount(account);

        return user;
    }

    private LocalDate generateRandomBirthDate() {
        java.util.Date date = faker.date().birthday(18, 80);
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private String generateUniqueEmail(String name, int index) {
        String cleanName = name.toLowerCase().replace(" ", ".");
        String domain = faker.options().option("mail.ru", "gmail.com", "yandex.ru", "bk.ru");
        return cleanName + index + "@" + domain;
    }

    private String generatePhone() {
        return "79" + faker.number().digits(9);
    }

    private BigDecimal generateRandomBalance() {
        double amount = faker.number().randomDouble(2, 100, 100000);
        return BigDecimal.valueOf(amount).setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    // Для отчётов — получение данных в разных форматах
    public List<Object[]> getUserDataForReport() {
        // TODO: будет позже для CSV/PDF
        return new ArrayList<>();
    }
}