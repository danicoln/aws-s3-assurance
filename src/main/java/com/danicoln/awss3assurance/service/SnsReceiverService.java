package com.danicoln.awss3assurance.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.ConfirmSubscriptionRequest;

@Service
public class SnsReceiverService {

    private static final Logger log = LoggerFactory.getLogger(SnsReceiverService.class);

    private final SnsClient snsClient;

    public SnsReceiverService(SnsClient snsClient) {
        this.snsClient = snsClient;
    }

    public String confirmSubscription(JsonNode payload) {
        String topicArn = textValue(payload, "TopicArn");
        String token = textValue(payload, "Token");

        snsClient.confirmSubscription(ConfirmSubscriptionRequest.builder()
                .topicArn(topicArn)
                .token(token)
                .build());

        log.info("SNS subscription confirmed: topicArn={}", topicArn);
        return "Subscription confirmed";
    }

    public String processNotification(JsonNode payload) {
        String topicArn = textValue(payload, "TopicArn");
        String messageId = textValue(payload, "MessageId");
        String message = textValue(payload, "Message");

        log.info("SNS notification received: topicArn={}, messageId={}, message={}", topicArn, messageId, message);
        return "Notification processed";
    }

    private String textValue(JsonNode payload, String fieldName) {
        JsonNode node = payload.get(fieldName);
        if (node == null || node.asText().isBlank()) {
            throw new IllegalArgumentException("SNS payload missing field: " + fieldName);
        }
        return node.asText();
    }
}
