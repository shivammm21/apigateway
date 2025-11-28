package com.apigateway.service;

import com.apigateway.model.RateLimitConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RateLimitService {
    private final StringRedisTemplate redis;

    private static final String KEY_USER_COUNTER = "rate:user:"; // + id
    private static final String KEY_IP_COUNTER = "rate:ip:";     // + ip
    private static final String KEY_CONFIG_LIMIT = "rate:config:limitPerMinute";

    public int getGlobalLimit() {
        String v = redis.opsForValue().get(KEY_CONFIG_LIMIT);
        return v == null ? 200 : Integer.parseInt(v);
    }

    public RateLimitConfig updateGlobal(RateLimitConfig cfg) {
        redis.opsForValue().set(KEY_CONFIG_LIMIT, String.valueOf(cfg.getLimitPerMinute()));
        return cfg;
    }

    public Map<String, Object> statusForUser(String userId) {
        String key = KEY_USER_COUNTER + userId + ":" + currentMinuteWindow();
        int used = getInt(redis.opsForValue().get(key));
        Map<String, Object> map = new HashMap<>();
        map.put("userId", userId);
        map.put("used", used);
        map.put("limit", getGlobalLimit());
        return map;
    }

    public boolean allowForUser(String userId) {
        String key = KEY_USER_COUNTER + userId + ":" + currentMinuteWindow();
        return incrementWithinWindow(key) <= getGlobalLimit();
    }

    public boolean allowForIp(String ip) {
        String key = KEY_IP_COUNTER + ip + ":" + currentMinuteWindow();
        return incrementWithinWindow(key) <= getGlobalLimit();
    }

    private long incrementWithinWindow(String key) {
        Long val = redis.opsForValue().increment(key);
        // set TTL to end of minute if first time
        if (val != null && val == 1L) {
            long ttlMs = millisUntilNextMinute();
            redis.expire(key, ttlMs, TimeUnit.MILLISECONDS);
        }
        return val == null ? 0 : val;
    }

    private String currentMinuteWindow() {
        long epochMin = System.currentTimeMillis() / 60000L;
        return String.valueOf(epochMin);
    }

    private long millisUntilNextMinute() {
        long now = System.currentTimeMillis();
        long next = ((now / 60000L) + 1) * 60000L;
        return Math.max(1000L, next - now);
    }

    private int getInt(String s) {
        try { return s == null ? 0 : Integer.parseInt(s); } catch (Exception e) { return 0; }
    }
}
