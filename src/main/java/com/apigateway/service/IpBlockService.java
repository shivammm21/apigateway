package com.apigateway.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class IpBlockService {
    private final StringRedisTemplate redis;
    private static final String KEY_BLOCK_SET = "block:ips";

    public boolean isBlocked(String ip) {
        Boolean member = redis.opsForSet().isMember(KEY_BLOCK_SET, ip);
        return member != null && member;
    }

    public void block(String ip) {
        redis.opsForSet().add(KEY_BLOCK_SET, ip);
    }

    public void unblock(String ip) {
        redis.opsForSet().remove(KEY_BLOCK_SET, ip);
    }

    public Set<String> list() {
        return redis.opsForSet().members(KEY_BLOCK_SET);
    }
}
