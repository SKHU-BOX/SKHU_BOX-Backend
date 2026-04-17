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
        long now = System.currentTimeMillis();

        // 1. Redis Sorted Set에 추가 (score는 현재 시간 -> 선착순)
        redisTemplate.opsForZSet().add(key, studentNumber, now);

        // 2. 현재 내 순위 가져오기 (0부터 시작하므로 +1)
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

    @Override
    public boolean isFirstUser(String studentNumber, Long lockerId) {
        String key = QUEUE_KEY_PREFIX + lockerId;
        // zrange(key, 0, 0) 으로 가장 첫 번째 사용자 확인
        java.util.Set<String> members = redisTemplate.opsForZSet().range(key, 0, 0);
        if (members == null || members.isEmpty()) {
            return false;
        }
        return members.iterator().next().equals(studentNumber);
    }

    @Override
    public void removeFromQueue(String studentNumber, Long lockerId) {
        String key = QUEUE_KEY_PREFIX + lockerId;
        redisTemplate.opsForZSet().remove(key, studentNumber);
    }
}
