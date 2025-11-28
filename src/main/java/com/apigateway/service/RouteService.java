package com.apigateway.service;

import com.apigateway.model.RouteConfigModel;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class RouteService {
    private final StringRedisTemplate redis;
    private static final String KEY_ROUTES = "routes"; // hash: pattern -> serviceUrl

    public List<RouteConfigModel> list() {
        HashOperations<String, String, String> ops = redis.opsForHash();
        Map<String, String> all = ops.entries(KEY_ROUTES);
        List<RouteConfigModel> res = new ArrayList<>();
        for (Map.Entry<String, String> e : all.entrySet()) {
            res.add(new RouteConfigModel(e.getKey(), e.getValue()));
        }
        res.sort(Comparator.comparing(RouteConfigModel::getPath));
        return res;
    }

    public void add(RouteConfigModel model) {
        redis.opsForHash().put(KEY_ROUTES, model.getPath(), model.getServiceUrl());
    }

    public Optional<String> resolveServiceUrl(String requestPath) {
        Map<Object, Object> routes = redis.opsForHash().entries(KEY_ROUTES);
        // simple wildcard match for /** suffix: convert '/users/**' to '/users/' prefix check
        String best = null;
        int bestLen = -1;
        for (Map.Entry<Object, Object> e : routes.entrySet()) {
            String pattern = e.getKey().toString();
            String base = pattern.replace("/**", "/");
            if (!base.endsWith("/")) base = base + "/";
            if (requestPath.startsWith(base) && base.length() > bestLen) {
                best = e.getValue().toString();
                bestLen = base.length();
            }
        }
        return Optional.ofNullable(best);
    }
}
