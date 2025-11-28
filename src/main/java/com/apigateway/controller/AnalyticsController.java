package com.apigateway.controller;

import com.apigateway.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/admin/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/traffic")
    public ResponseEntity<Map<Object, Object>> traffic() {
        return ResponseEntity.ok(analyticsService.traffic());
    }

    @GetMapping("/errors")
    public ResponseEntity<Map<Object, Object>> errors() {
        return ResponseEntity.ok(analyticsService.errors());
    }

    @GetMapping("/services")
    public ResponseEntity<Map<Object, Object>> services() {
        return ResponseEntity.ok(analyticsService.services());
    }
}
