package com.apigateway.controller;

import com.apigateway.service.LogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/logs")
@RequiredArgsConstructor
public class LogController {

    private final LogService logService;

    @GetMapping("/recent")
    public ResponseEntity<List<String>> recent() {
        return ResponseEntity.ok(logService.recent());
    }
}
