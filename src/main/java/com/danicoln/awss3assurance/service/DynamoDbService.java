package com.danicoln.awss3assurance.service;

import com.danicoln.awss3assurance.exception.ResourceConflictException;
import com.danicoln.awss3assurance.exception.ResourceNotFoundException;
import com.danicoln.awss3assurance.properties.AwsStorageProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
public class DynamoDbService {

    private static final Logger log = LoggerFactory.getLogger(DynamoDbService.class);

    private final DynamoDbClient dynamoDbClient;
    private final AwsStorageProperties properties;

    public DynamoDbService(DynamoDbClient dynamoDbClient, AwsStorageProperties properties) {
        this.dynamoDbClient = dynamoDbClient;
        this.properties = properties;
    }

    public List<DynamoItem> list() {
        String tableName = tableName();
        ScanRequest request = ScanRequest.builder()
                .tableName(tableName)
                .build();

        List<DynamoItem> items = dynamoDbClient.scan(request)
                .items()
                .stream()
                .map(this::toItem)
                .sorted(Comparator.comparing(DynamoItem::id))
                .toList();

        log.info("DynamoDB list completed: table={}, count={}", tableName, items.size());
        return items;
    }

    public DynamoItem create(DynamoItem item) {
        validate(item);
        ensureMissing(item.id());

        String tableName = tableName();
        dynamoDbClient.putItem(PutItemRequest.builder()
                .tableName(tableName)
                .item(toAttributes(item))
                .build());

        log.info("DynamoDB item created: table={}, id={}", tableName, item.id());
        return item;
    }

    public DynamoItem update(String id, String name) {
        ensureHasText(id, "Item id is required");
        ensureHasText(name, "Item name is required");
        ensureExisting(id);

        String tableName = tableName();
        dynamoDbClient.updateItem(UpdateItemRequest.builder()
                .tableName(tableName)
                .key(key(id))
                .updateExpression("SET #name = :name")
                .expressionAttributeNames(Map.of("#name", "name"))
                .expressionAttributeValues(Map.of(":name", AttributeValue.builder().s(name).build()))
                .build());

        log.info("DynamoDB item updated: table={}, id={}", tableName, id);
        return new DynamoItem(id, name);
    }

    public void delete(String id) {
        ensureHasText(id, "Item id is required");
        ensureExisting(id);

        String tableName = tableName();
        dynamoDbClient.deleteItem(DeleteItemRequest.builder()
                .tableName(tableName)
                .key(key(id))
                .build());

        log.info("DynamoDB item deleted: table={}, id={}", tableName, id);
    }

    private DynamoItem toItem(Map<String, AttributeValue> attributes) {
        return new DynamoItem(
                value(attributes, "id"),
                value(attributes, "name")
        );
    }

    private Map<String, AttributeValue> toAttributes(DynamoItem item) {
        return Map.of(
                "id", AttributeValue.builder().s(item.id()).build(),
                "name", AttributeValue.builder().s(item.name()).build()
        );
    }

    private void ensureMissing(String id) {
        if (exists(id)) {
            throw new ResourceConflictException("Item with id '" + id + "' already exists");
        }
    }

    private void ensureExisting(String id) {
        if (!exists(id)) {
            throw new ResourceNotFoundException("Item with id '" + id + "' was not found");
        }
    }

    private boolean exists(String id) {
        String tableName = tableName();
        GetItemRequest request = GetItemRequest.builder()
                .tableName(tableName)
                .key(key(id))
                .build();

        return dynamoDbClient.getItem(request).hasItem();
    }

    private Map<String, AttributeValue> key(String id) {
        return Map.of("id", AttributeValue.builder().s(id).build());
    }

    private String value(Map<String, AttributeValue> attributes, String key) {
        AttributeValue value = attributes.get(key);
        return value == null ? null : value.s();
    }

    private void validate(DynamoItem item) {
        if (item == null) {
            throw new IllegalArgumentException("Request body is required");
        }
        ensureHasText(item.id(), "Item id is required");
        ensureHasText(item.name(), "Item name is required");
    }

    private void ensureHasText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }

    private String tableName() {
        String tableName = properties.getDynamodb().getTableName();
        if (tableName == null || tableName.isBlank()) {
            throw new IllegalStateException("Missing configuration for app.aws.dynamodb.table-name or AWS_DYNAMODB_TABLE");
        }
        return tableName;
    }

    public record DynamoItem(String id, String name) {
    }
}
