package com.bank.controller;

import com.bank.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Tag(name = "Отчёты", description = "Генерация отчётов в CSV и PDF")
@SecurityRequirement(name = "Bearer Authentication")
public class ReportController {

    private final ReportService reportService;

    @Operation(summary = "Скачать отчёт в указанном формате", description = "Поддерживаемые форматы: csv, pdf")
    @GetMapping(produces = "application/octet-stream")
    public ResponseEntity<byte[]> exportReport(
            @Parameter(description = "Формат отчёта (csv, pdf)", example = "csv")
            @RequestParam(defaultValue = "csv") String format) {

        log.info("Exporting report in {} format", format);

        byte[] content = reportService.generateReport(format);
        String contentType = reportService.getContentType(format);
        String extension = reportService.getFileExtension(format);

        String filename = "users_report_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + extension;

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType(contentType))
                .body(content);
    }
}