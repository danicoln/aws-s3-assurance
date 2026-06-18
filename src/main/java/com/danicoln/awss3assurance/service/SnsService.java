package com.danicoln.awss3assurance.service;

import com.danicoln.awss3assurance.properties.AwsStorageProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.MessageAttributeValue;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;
import software.amazon.awssdk.services.sns.model.SubscribeRequest;
import software.amazon.awssdk.services.sns.model.SubscribeResponse;

import java.util.Map;

@Service
public class SnsService {

    private static final Logger log = LoggerFactory.getLogger(SnsService.class);

    private final SnsClient snsClient;
    private final AwsStorageProperties properties;

    public SnsService(SnsClient snsClient, AwsStorageProperties properties) {
        this.snsClient = snsClient;
        this.properties = properties;
    }

    public PublishResult publish(String message, String subject) {
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("Message is required");
        }

        String topicArn = topicArn();
        PublishRequest.Builder request = PublishRequest.builder()
                .topicArn(topicArn)
                .message(message);

        if (subject != null && !subject.isBlank()) {
            request.subject(subject);
        }

        PublishResponse response = snsClient.publish(request.build());
        log.info("SNS message published: topicArn={}, messageId={}", topicArn, response.messageId());
        return new PublishResult(response.messageId());
    }

    public SubscriptionResult subscribeEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }

        return subscribe("email", email);
    }

    public SubscriptionResult subscribeHttp(String endpointUrl) {
        if (endpointUrl == null || endpointUrl.isBlank()) {
            throw new IllegalArgumentException("Endpoint URL is required");
        }

        String protocol = endpointUrl.startsWith("https://") ? "https" : "http";
        return subscribe(protocol, endpointUrl);
    }

    public PublishResult sendSms(String phoneNumber, String message) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            throw new IllegalArgumentException("Phone number is required");
        }
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("Message is required");
        }

        PublishResponse response = snsClient.publish(PublishRequest.builder()
                .phoneNumber(phoneNumber)
                .message(message)
                .messageAttributes(Map.of(
                        "AWS.SNS.SMS.SMSType",
                        MessageAttributeValue.builder()
                                .dataType("String")
                                .stringValue("Transactional")
                                .build()
                ))
                .build());

        log.info("SNS SMS sent: phoneNumber={}, messageId={}", phoneNumber, response.messageId());
        return new PublishResult(response.messageId());
    }

    private SubscriptionResult subscribe(String protocol, String endpoint) {
        String topicArn = topicArn();
        SubscribeResponse response = snsClient.subscribe(SubscribeRequest.builder()
                .topicArn(topicArn)
                .protocol(protocol)
                .endpoint(endpoint)
                .build());

        log.info("SNS subscription requested: topicArn={}, protocol={}, endpoint={}", topicArn, protocol, endpoint);
        return new SubscriptionResult(response.subscriptionArn());
    }

    private String topicArn() {
        String topicArn = properties.getSns().getTopicArn();
        if (topicArn == null || topicArn.isBlank()) {
            throw new IllegalStateException("Missing configuration for app.aws.sns.topic-arn or AWS_SNS_TOPIC_ARN");
        }
        return topicArn;
    }

    public record PublishResult(String messageId) {
    }

    public record SubscriptionResult(String subscriptionArn) {
    }
}
