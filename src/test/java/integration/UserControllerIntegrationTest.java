package integration;

import com.bank.BankApplication;
import com.bank.dto.request.LoginRequest;
import com.bank.dto.response.JwtResponse;
import com.bank.entity.Account;
import com.bank.entity.EmailData;
import com.bank.entity.PhoneData;
import com.bank.entity.User;
import com.bank.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@SpringBootTest(classes = BankApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
class UserControllerIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:14")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.cache.type", () -> "none");
        registry.add("spring.sql.init.mode", () -> "always");  // ← Включаем schema.sql
        registry.add("jwt.secret", () -> "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970");
        registry.add("jwt.expiration", () -> "86400000");
        registry.add("spring.task.scheduling.enabled", () -> "false");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        User user = new User();
        user.setId(1L);
        user.setName("Иван Петров");
        user.setDateOfBirth(LocalDate.of(1990, 5, 15));
        user.setPassword(passwordEncoder.encode("password123"));

        userRepository.save(user);

        EmailData email = EmailData.builder()
                .email("ivan@mail.ru")
                .user(user)
                .build();

        PhoneData phone = PhoneData.builder()
                .phone("79201234567")
                .user(user)
                .build();

        if (user.getEmails() == null) {
            user.setEmails(new ArrayList<>());
        }
        user.getEmails().add(email);

        if (user.getPhones() == null) {
            user.setPhones(new ArrayList<>());
        }
        user.getPhones().add(phone);

        Account account = Account.builder()
                .user(user)
                .balance(new BigDecimal("1000.00"))
                .initialBalance(new BigDecimal("1000.00"))
                .build();

        user.setAccount(account);
        userRepository.save(user);
    }


    @Test
    void testSearchWithoutToken_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/users/search")
                        .param("name", "Иван"))
                .andExpect(status().isUnauthorized());
    }
}