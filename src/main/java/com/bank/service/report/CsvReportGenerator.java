package com.bank.service.report;

import com.bank.dto.response.UserReportDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Component
public class CsvReportGenerator implements ReportGenerator {

    @Override
    public String getType() {
        return "csv";
    }

    @Override
    public byte[] generate(List<UserReportDto> users) {
        log.info("Generating CSV report for {} users", users.size());

        StringBuilder sb = new StringBuilder();

        // Заголовки
        sb.append("ID;Имя;Дата рождения;Emails;Телефоны;Баланс;Начальный баланс\n");

        // Данные
        for (UserReportDto user : users) {
            sb.append(user.getUserId()).append(";")
                    .append(escapeCsv(user.getName())).append(";")
                    .append(user.getDateOfBirth()).append(";")
                    .append(user.getEmails()).append(";")
                    .append(user.getPhones()).append(";")
                    .append(user.getBalance()).append(";")
                    .append(user.getInitialBalance()).append("\n");
        }

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public String getContentType() {
        return "text/csv; charset=UTF-8";
    }

    @Override
    public String getFileExtension() {
        return ".csv";
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(";") || value.contains("\"")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
