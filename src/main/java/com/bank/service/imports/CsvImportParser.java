package com.bank.service.imports;

import com.bank.dto.request.ImportUserDto;
import com.bank.exception.ImportException;
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

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

            String line;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;

                // Пропускаем заголовок
                if (lineNumber == 1 && line.startsWith("ID") || line.startsWith("Имя")) {
                    continue;
                }

                String[] columns = line.split(DELIMITER);
                if (columns.length < 6) {
                    log.warn("Line {} has insufficient columns: {}", lineNumber, columns.length);
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
                    log.error("Error parsing line {}: {}", lineNumber, e.getMessage());
                    throw new ImportException("Error parsing line " + lineNumber + ": " + e.getMessage());
                }
            }

            log.info("Parsed {} users from CSV", users.size());
            return users;

        } catch (Exception e) {
            log.error("Failed to parse CSV", e);
            throw new ImportException("Failed to parse CSV: " + e.getMessage(), e);
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
