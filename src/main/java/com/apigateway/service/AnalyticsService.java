package com.apigateway.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AnalyticsService {
    private final StringRedisTemplate redis;

    private static final String KEY_TRAFFIC = "traffic:stats"; // hash fields like svc:<name>, user:<id>
    private static final String KEY_ERRORS = "errors:stats";   // fields: 4xx, 5xx, jwt, routing
    private static final String KEY_SERVICES = "services:status"; // fields: service -> UP/DOWN

    public void incTrafficForService(String serviceName) {
        redis.opsForHash().increment(KEY_TRAFFIC, "svc:" + serviceName, 1);
    }

    public void incTrafficForUser(String userId) {
        redis.opsForHash().increment(KEY_TRAFFIC, "user:" + userId, 1);
    }

    public void incError(String kind) {
        redis.opsForHash().increment(KEY_ERRORS, kind, 1);
    }

    public Map<Object, Object> traffic() { return redis.opsForHash().entries(KEY_TRAFFIC); }

    public Map<Object, Object> errors() { return redis.opsForHash().entries(KEY_ERRORS); }

    public Map<Object, Object> services() { return redis.opsForHash().entries(KEY_SERVICES); }

    public void setServiceStatus(String name, String status) {
        redis.opsForHash().put(KEY_SERVICES, name, status);
    }
}
