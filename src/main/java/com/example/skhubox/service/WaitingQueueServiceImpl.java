package com.example.skhubox.service;

import com.example.skhubox.dto.QueueResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WaitingQueueServiceImpl implements WaitingQueueService {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String QUEUE_KEY_PREFIX = "locker:queue:";

    @Override
    public QueueResponse register(String studentNumber, Long lockerId) {
        String key = QUEUE_KEY_PREFIX + lockerId;
        
        // 1. 이미 등록된 유저인지 확인 (순번이 밀리는 것을 방지)
        Long existingRank = redisTemplate.opsForZSet().rank(key, studentNumber);
        if (existingRank != null) {
            return QueueResponse.of(lockerId, existingRank + 1, "이미 대기열에 등록되어 있습니다.");
        }

        // 2. 신규 유저만 현재 시간 점수로 추가
        long now = System.currentTimeMillis();
        redisTemplate.opsForZSet().add(key, studentNumber, now);

        // 3. 순위 반환
        Long rank = redisTemplate.opsForZSet().rank(key, studentNumber);
        long displayRank = (rank != null) ? rank + 1 : 0;

        return QueueResponse.of(lockerId, displayRank, "대기열에 성공적으로 등록되었습니다.");
    }

    @Override
    public Long getRank(String studentNumber, Long lockerId) {
        String key = QUEUE_KEY_PREFIX + lockerId;
        Long rank = redisTemplate.opsForZSet().rank(key, studentNumber);
        return (rank != null) ? rank + 1 : null;
    }
}
