package com.apigateway.controller;

import com.apigateway.model.RateLimitConfig;
import com.apigateway.service.RateLimitService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin/rate-limit")
@RequiredArgsConstructor
public class RateLimitController {

    private final RateLimitService rateLimitService;

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> status(@RequestParam("userId") String userId) {
        return ResponseEntity.ok(rateLimitService.statusForUser(userId));
    }

    @PostMapping("/update")
    public ResponseEntity<RateLimitConfig> update(@RequestBody RateLimitConfig config) {
        return ResponseEntity.ok(rateLimitService.updateGlobal(config));
    }
}
