package com.apigateway.controller;

import com.apigateway.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtService jwtService;

    @GetMapping("/validate")
    public ResponseEntity<Map<String, Object>> validate(@RequestHeader(value = "Authorization", required = false) String authorization) {
        Map<String, Object> res = jwtService.validate(authorization);
        return ResponseEntity.ok(res);
    }
}
