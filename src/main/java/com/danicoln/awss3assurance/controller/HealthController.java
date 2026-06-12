package com.danicoln.awss3assurance.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
public class HealthController {

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> checkHealth() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "aws-s3-assurance",
                "timestamp", Instant.now().toString()
        ));
    }
}
