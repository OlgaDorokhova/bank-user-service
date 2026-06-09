package com.bank.controller;

import com.bank.exception.CustomExceptions.ImportException;
import com.bank.service.ImportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/imports")
@RequiredArgsConstructor
@Tag(name = "Импорт данных", description = "Импорт пользователей из файлов (CSV, Excel)")
@SecurityRequirement(name = "Bearer Authentication")
public class ImportController {

    private final ImportService importService;

    @Operation(summary = "Импорт пользователей из CSV файла")
    @PostMapping(value = "/csv", consumes = "multipart/form-data")
    public ResponseEntity<Map<String, Object>> importCsv(
            @Parameter(description = "CSV файл с данными пользователей (разделитель ;)")
            @RequestParam("file") MultipartFile file) {

        log.info("Received CSV import request: filename={}, size={}",
                file.getOriginalFilename(), file.getSize());

        validateFile(file, "csv");

        int importedCount = importService.importUsers(file, "csv");

        return ResponseEntity.ok(Map.of(
                "message", "Import completed successfully",
                "importedCount", importedCount,
                "filename", file.getOriginalFilename(),
                "format", "csv"
        ));
    }

    @Operation(summary = "Импорт пользователей из Excel файла (.xlsx)")
    @PostMapping(value = "/excel", consumes = "multipart/form-data")
    public ResponseEntity<Map<String, Object>> importExcel(
            @Parameter(description = "Excel файл (.xlsx) с данными пользователей")
            @RequestParam("file") MultipartFile file) {

        log.info("Received Excel import request: filename={}, size={}",
                file.getOriginalFilename(), file.getSize());

        validateFile(file, "xlsx");

        int importedCount = importService.importUsers(file, "xlsx");

        return ResponseEntity.ok(Map.of(
                "message", "Import completed successfully",
                "importedCount", importedCount,
                "filename", file.getOriginalFilename(),
                "format", "excel"
        ));
    }

    private void validateFile(MultipartFile file, String expectedExtension) {
        if (file.isEmpty()) {
            throw new ImportException("File is empty");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith("." + expectedExtension)) {
            throw new ImportException("Invalid file type. Expected ." + expectedExtension + ", got: " + originalFilename);
        }
    }
}