package com.bank.service.imports;

import com.bank.dto.request.ImportUserDto;
import com.bank.exception.ImportException;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class ExcelImportParser implements ImportParser {

    private static final String TYPE = "xlsx";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public List<ImportUserDto> parse(InputStream inputStream) throws ImportException {
        List<ImportUserDto> users = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                int rowNum = row.getRowNum();

                // Пропускаем заголовок
                if (rowNum == 0) {
                    continue;
                }

                try {
                    ImportUserDto user = parseRow(row);
                    if (user != null && user.getName() != null && !user.getName().isEmpty()) {
                        users.add(user);
                    }
                } catch (Exception e) {
                    log.error("Error parsing row {}: {}", rowNum, e.getMessage());
                    throw new ImportException("Error parsing row " + (rowNum + 1) + ": " + e.getMessage());
                }
            }

            log.info("Parsed {} users from Excel", users.size());
            return users;

        } catch (Exception e) {
            log.error("Failed to parse Excel", e);
            throw new ImportException("Failed to parse Excel: " + e.getMessage(), e);
        }
    }

    private ImportUserDto parseRow(Row row) {
        ImportUserDto.ImportUserDtoBuilder builder = ImportUserDto.builder();

        // Колонка 0: Имя
        Cell nameCell = row.getCell(0, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
        builder.name(getStringValue(nameCell));

        // Колонка 1: Дата рождения
        Cell dateCell = row.getCell(1, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
        builder.dateOfBirth(parseDate(getStringValue(dateCell)));

        // Колонка 2: Email
        Cell emailCell = row.getCell(2, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
        builder.email(getStringValue(emailCell));

        // Колонка 3: Телефон
        Cell phoneCell = row.getCell(3, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
        builder.phone(getStringValue(phoneCell));

        // Колонка 4: Начальный баланс
        Cell balanceCell = row.getCell(4, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
        builder.initialBalance(getNumericValue(balanceCell));

        return builder.build();
    }

    private String getStringValue(Cell cell) {
        if (cell == null) return "";

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                return String.valueOf((long) cell.getNumericCellValue());
            default:
                return "";
        }
    }

    private BigDecimal getNumericValue(Cell cell) {
        if (cell == null) return BigDecimal.ZERO;

        switch (cell.getCellType()) {
            case NUMERIC:
                return BigDecimal.valueOf(cell.getNumericCellValue());
            case STRING:
                try {
                    return new BigDecimal(cell.getStringCellValue().trim());
                } catch (NumberFormatException e) {
                    return BigDecimal.ZERO;
                }
            default:
                return BigDecimal.ZERO;
        }
    }

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return null;

        try {
            return LocalDate.parse(dateStr, DATE_FORMATTER);
        } catch (Exception e) {
            try {
                return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("dd.MM.yyyy"));
            } catch (Exception ex) {
                log.warn("Could not parse date: {}", dateStr);
                return null;
            }
        }
    }
}
