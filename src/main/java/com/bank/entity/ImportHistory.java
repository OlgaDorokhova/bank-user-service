package com.bank.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "import_history")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "filename", nullable = false, length = 255)
    private String filename;

    @Column(name = "format", nullable = false, length = 10)
    private String format;  // csv, xlsx

    @Column(name = "total_records", nullable = false)
    private int totalRecords;

    @Column(name = "successful_records", nullable = false)
    private int successfulRecords;

    @Column(name = "failed_records", nullable = false)
    private int failedRecords;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private ImportStatus status;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "imported_by", nullable = false)
    private Long importedBy;  // userId

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public enum ImportStatus {
        SUCCESS,
        PARTIAL_SUCCESS,
        FAILED
    }
}
