package com.example.skhubox.service;

import com.example.skhubox.domain.operation.OperationLog;
import com.example.skhubox.domain.operation.OperationLogType;
import com.example.skhubox.repository.OperationLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OperationLogService {

    private final OperationLogRepository operationLogRepository;

    @Transactional
    public void log(OperationLogType type, String title, String description) {
        operationLogRepository.save(new OperationLog(type, title, description));
    }
}
