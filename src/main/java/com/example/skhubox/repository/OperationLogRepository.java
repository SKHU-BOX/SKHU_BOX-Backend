package com.example.skhubox.repository;

import com.example.skhubox.domain.operation.OperationLog;
import com.example.skhubox.domain.operation.OperationLogType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface OperationLogRepository extends JpaRepository<OperationLog, Long> {
    long countByTypeInAndCreatedAtBetween(Collection<OperationLogType> types, LocalDateTime start, LocalDateTime end);
    List<OperationLog> findTop4ByTypeInOrderByCreatedAtDesc(Collection<OperationLogType> types);
    List<OperationLog> findTop10ByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime start, LocalDateTime end);
}
