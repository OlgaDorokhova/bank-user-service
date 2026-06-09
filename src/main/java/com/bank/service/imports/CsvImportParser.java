package com.bank.service.imports;

import com.bank.dto.request.ImportUserDto;
import com.bank.exception.CustomExceptions.ImportException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class CsvImportParser implements ImportParser {

    private static final String TYPE = "csv";
    private static final String DELIMITER = ";";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public List<ImportUserDto> parse(InputStream inputStream) throws ImportException {
        List<ImportUserDto> users = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        int lineNumber = 0;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

            String line;

            while ((line = reader.readLine()) != null) {
                lineNumber++;

                // Пропускаем заголовок
                if (lineNumber == 1 && line.startsWith("Имя")) {
                    continue;
                }

                String[] columns = line.split(DELIMITER);
                if (columns.length < 5) {
                    errors.add("Line " + lineNumber + ": Expected 5 columns, got " + columns.length);
                    continue;
                }

                try {
                    ImportUserDto user = ImportUserDto.builder()
                            .name(columns[0].trim())
                            .dateOfBirth(parseDate(columns[1].trim()))
                            .email(columns[2].trim())
                            .phone(columns[3].trim())
                            .initialBalance(new BigDecimal(columns[4].trim()))
                            .build();

                    users.add(user);

                } catch (Exception e) {
                    errors.add("Line " + lineNumber + ": " + e.getMessage());
                }
            }

            if (!errors.isEmpty()) {
                log.warn("CSV parsing warnings: {}", errors);
                // Можно выбросить исключение или продолжить
            }

            log.info("Parsed {} users from CSV, {} errors", users.size(), errors.size());
            return users;

        } catch (Exception e) {
            throw new ImportException("Failed to parse CSV: " + e.getMessage());
        }
    }

    private LocalDate parseDate(String dateStr) {
        try {
            return LocalDate.parse(dateStr, DATE_FORMATTER);
        } catch (Exception e) {
            // Попробуем другой формат
            return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        }
    }
}
