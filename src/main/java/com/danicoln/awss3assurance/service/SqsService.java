package com.danicoln.awss3assurance.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.danicoln.awss3assurance.properties.AwsStorageProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

@Service
public class SqsService {

    private static final Logger log = LoggerFactory.getLogger(SqsService.class);

    private final SqsClient sqsClient;
    private final AwsStorageProperties properties;
    private final ObjectMapper objectMapper;

    public SqsService(SqsClient sqsClient, AwsStorageProperties properties, ObjectMapper objectMapper) {
        this.sqsClient = sqsClient;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    public MessageResult sendMessage(String messageBody) {
        if (messageBody == null || messageBody.isBlank()) {
            throw new IllegalArgumentException("Message body is required");
        }

        String queueUrl = queueUrl();
        SendMessageResponse response = sqsClient.sendMessage(SendMessageRequest.builder()
                .queueUrl(queueUrl)
                .messageBody(messageBody)
                .build());

        log.info("SQS message sent: queueUrl={}, messageId={}", queueUrl, response.messageId());
        return new MessageResult(response.messageId(), response.sequenceNumber());
    }

    public MessageResult sendMessage(Object payload) {
        try {
            return sendMessage(objectMapper.writeValueAsString(payload));
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("Unable to serialize SQS payload", exception);
        }
    }

    private String queueUrl() {
        String queueUrl = properties.getSqs().getQueueUrl();
        if (queueUrl == null || queueUrl.isBlank()) {
            throw new IllegalStateException("Missing configuration for app.aws.sqs.queue-url or AWS_SQS_QUEUE_URL");
        }
        return queueUrl;
    }

    public record MessageResult(String messageId, String sequenceNumber) {
    }
}
