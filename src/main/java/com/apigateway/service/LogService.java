package com.apigateway.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LogService {
    private final StringRedisTemplate redis;
    private static final String KEY_LOGS = "logs:recent";
    private static final int MAX = 100;

    public void push(String json) {
        redis.opsForList().leftPush(KEY_LOGS, json);
        redis.opsForList().trim(KEY_LOGS, 0, MAX - 1);
    }

    public List<String> recent() {
        return redis.opsForList().range(KEY_LOGS, 0, MAX - 1);
    }
}
