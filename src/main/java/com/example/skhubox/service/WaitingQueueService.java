package com.example.skhubox.service;

import com.example.skhubox.dto.QueueResponse;

public interface WaitingQueueService {
    QueueResponse register(String studentNumber, Long lockerId);
    Long getRank(String studentNumber, Long lockerId);
}
