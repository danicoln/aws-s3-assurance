package com.danicoln.awss3assurance.controller;

import com.danicoln.awss3assurance.service.SnsReceiverService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sns/receiver")
public class SnsReceiverController {

    private static final Logger log = LoggerFactory.getLogger(SnsReceiverController.class);

    private final SnsReceiverService snsReceiverService;
    private final ObjectMapper objectMapper;

    public SnsReceiverController(SnsReceiverService snsReceiverService, ObjectMapper objectMapper) {
        this.snsReceiverService = snsReceiverService;
        this.objectMapper = objectMapper;
    }

    @PostMapping
    public ResponseEntity<String> receive(@RequestBody String body) throws Exception {
        JsonNode payload = objectMapper.readTree(body);
        String type = payload.path("Type").asText();
        log.info("SNS callback received: type={}", type);

        return switch (type) {
            case "SubscriptionConfirmation" -> ResponseEntity.ok(snsReceiverService.confirmSubscription(payload));
            case "Notification" -> ResponseEntity.ok(snsReceiverService.processNotification(payload));
            default -> ResponseEntity.ok("Unknown type: " + type);
        };
    }
}
