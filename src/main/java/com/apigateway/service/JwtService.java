package com.apigateway.service;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class JwtService {

    public Map<String, Object> validate(String authorizationHeader) {
        Map<String, Object> result = new HashMap<>();
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            result.put("valid", false);
            return result;
        }
        String token = authorizationHeader.replaceFirst("(?i)Bearer ", "").trim();
        // TODO: Replace with real JWT validation (e.g., using a public key / secret)
        boolean valid = token.length() > 10; // naive stub
        result.put("valid", valid);
        if (valid) {
            // naive parsing for demo; in real life parse claims
            result.put("userId", "user-" + Math.abs(token.hashCode()));
            result.put("role", "USER");
        }
        return result;
    }
}
