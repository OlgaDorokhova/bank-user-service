package com.bank.repository;

import com.bank.entity.ImportHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImportHistoryRepository extends JpaRepository<ImportHistory, Long> {

    Page<ImportHistory> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<ImportHistory> findByImportedBy(Long userId, Pageable pageable);
}
