package com.danicoln.awss3assurance.controller;

import com.danicoln.awss3assurance.service.SnsService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sns")
public class SnsController {

    private final SnsService snsService;

    public SnsController(SnsService snsService) {
        this.snsService = snsService;
    }

    @PostMapping("/publish")
    @ResponseStatus(HttpStatus.CREATED)
    public PublishResponse publish(@RequestBody PublishRequest request) {
        SnsService.PublishResult result = snsService.publish(request.message(), request.subject());
        return new PublishResponse(result.messageId());
    }

    @PostMapping("/subscribe")
    @ResponseStatus(HttpStatus.CREATED)
    public SubscribeResponse subscribeEmail(@RequestBody SubscribeEmailRequest request) {
        SnsService.SubscriptionResult result = snsService.subscribeEmail(request.email());
        return new SubscribeResponse(result.subscriptionArn());
    }

    @PostMapping("/subscribe-app")
    @ResponseStatus(HttpStatus.CREATED)
    public SubscribeResponse subscribeApp(@RequestBody SubscribeAppRequest request) {
        SnsService.SubscriptionResult result = snsService.subscribeHttp(request.endpointUrl());
        return new SubscribeResponse(result.subscriptionArn());
    }

    @PostMapping("/sms")
    @ResponseStatus(HttpStatus.CREATED)
    public PublishResponse sendSms(@RequestBody SendSmsRequest request) {
        SnsService.PublishResult result = snsService.sendSms(request.phoneNumber(), request.message());
        return new PublishResponse(result.messageId());
    }

    public record PublishRequest(String message, String subject) {
    }

    public record PublishResponse(String messageId) {
    }

    public record SubscribeEmailRequest(String email) {
    }

    public record SubscribeAppRequest(String endpointUrl) {
    }

    public record SubscribeResponse(String subscriptionArn) {
    }

    public record SendSmsRequest(String phoneNumber, String message) {
    }
}
