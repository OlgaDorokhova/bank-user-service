package com.bank.dev;

import com.bank.entity.User;
import com.bank.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = "app.data-generator.enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class DevDataInitializer {

    @Value("${app.data-generator.user-count:50}")
    private int userCount;

    @Value("${app.data-generator.mode:ADD}")  // ADD, REPLACE, SKIP_IF_EXISTS
    private String mode;

    private final UserRepository userRepository;
    private final Optional<DataGenerator> dataGenerator;

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        if (dataGenerator.isEmpty()) {
            log.info("DataGenerator not available");
            return;
        }

        List<User> users = new ArrayList<>();

        switch (mode.toUpperCase()) {
            case "REPLACE":
                log.info("REPLACE mode — deleting all existing users...");
                userRepository.deleteAll();
                users = dataGenerator.get().generateUsers(userCount);
                break;

            case "ADD":
                log.info("ADD mode — generating additional users...");
                users = dataGenerator.get().generateUsers(userCount);
                break;

            case "SKIP_IF_EXISTS":
                if (userRepository.count() > 0) {
                    log.info("SKIP mode — users already exist ({} users), skipping", userRepository.count());
                    return;
                }
                log.info("SKIP mode — database empty, generating initial users...");
                users = dataGenerator.get().generateUsers(userCount);
                break;

            default:
                log.warn("Unknown mode: {}, using ADD mode", mode);
                users = dataGenerator.get().generateUsers(userCount);
        }

        log.info("🚀 Generating {} test users...", users.size());
        userRepository.saveAll(users);
        log.info("✅ Saved {} test users. Total users now: {}", users.size(), userRepository.count());
    }
}