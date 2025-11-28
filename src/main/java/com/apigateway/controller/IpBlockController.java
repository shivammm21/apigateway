package com.apigateway.controller;

import com.apigateway.model.BlockIpRequest;
import com.apigateway.service.IpBlockService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/admin/ip-block")
@RequiredArgsConstructor
public class IpBlockController {

    private final IpBlockService ipBlockService;

    @GetMapping("/list")
    public ResponseEntity<Set<String>> list() {
        return ResponseEntity.ok(ipBlockService.list());
    }

    @PostMapping("/block")
    public ResponseEntity<Void> block(@RequestBody BlockIpRequest req) {
        ipBlockService.block(req.getIp());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/unblock")
    public ResponseEntity<Void> unblock(@RequestBody BlockIpRequest req) {
        ipBlockService.unblock(req.getIp());
        return ResponseEntity.ok().build();
    }
}
