package com.pms.pattern_detector_sequence.service.impl;

import com.pms.pattern_detector_sequence.dto.KeystrokeMessage;
import com.pms.pattern_detector_sequence.service.KeystrokeBufferService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class KeystrokeBufferServiceImpl implements KeystrokeBufferService {

    private final RedisTemplate<String, KeystrokeMessage> redisTemplate;
    private static final String KEY_PREFIX = "keystroke:";
    private static final long DEFAULT_TTL = 86400; // 1 day in seconds

    @Override
    public void addEvent(String sessionId, KeystrokeMessage event) {
        String key = getKey(sessionId);
        log.info("Adding event to Redis - Key: {}, Event: {}", key, event);
        redisTemplate.opsForList().rightPush(key, event);
        redisTemplate.expire(key, java.time.Duration.ofSeconds(DEFAULT_TTL));
        log.info("Event added successfully to Redis");
    }

    @Override
    public List<KeystrokeMessage> getEvents(String sessionId) {
        String key = getKey(sessionId);
        log.info("Retrieving events from Redis - Key: {}", key);
        
        List<KeystrokeMessage> events = redisTemplate.opsForList().range(key, 0, -1);
        log.info("Retrieved {} events from Redis", events != null ? events.size() : 0);
        
        return events != null ? events : List.of();
    }

    @Override
    public void clearEvents(String sessionId) {
        String key = getKey(sessionId);
        log.info("Clearing events from Redis - Key: {}", key);
        redisTemplate.delete(key);
        log.info("Events cleared successfully");
    }

    private String getKey(String sessionId) {
        return KEY_PREFIX + sessionId;
    }
} 