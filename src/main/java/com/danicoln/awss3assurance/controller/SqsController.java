package com.danicoln.awss3assurance.controller;

import com.danicoln.awss3assurance.service.SqsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sqs")
public class SqsController {

    private final SqsService sqsService;

    public SqsController(SqsService sqsService) {
        this.sqsService = sqsService;
    }

    @PostMapping("/messages")
    @ResponseStatus(HttpStatus.CREATED)
    public SendMessageResponse sendMessage(@RequestBody SendMessageRequest request) {
        SqsService.MessageResult result = sqsService.sendMessage(request.messageBody());
        return new SendMessageResponse(result.messageId(), result.sequenceNumber());
    }

    public record SendMessageRequest(String messageBody) {
    }

    public record SendMessageResponse(String messageId, String sequenceNumber) {
    }
}
