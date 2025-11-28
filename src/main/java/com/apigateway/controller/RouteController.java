package com.apigateway.controller;

import com.apigateway.model.RouteConfigModel;
import com.apigateway.service.RouteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/routes")
@RequiredArgsConstructor
public class RouteController {

    private final RouteService routeService;

    @GetMapping("/list")
    public ResponseEntity<List<RouteConfigModel>> list() {
        return ResponseEntity.ok(routeService.list());
    }

    @PostMapping("/add")
    public ResponseEntity<Void> add(@RequestBody RouteConfigModel model) {
        routeService.add(model);
        return ResponseEntity.ok().build();
    }
}
