package com.danicoln.awss3assurance.controller;

import com.danicoln.awss3assurance.service.DynamoDbService;
import com.danicoln.awss3assurance.service.DynamoDbService.DynamoItem;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/dynamodb/items")
public class DynamoDbController {

    private final DynamoDbService dynamoDbService;

    public DynamoDbController(DynamoDbService dynamoDbService) {
        this.dynamoDbService = dynamoDbService;
    }

    @GetMapping
    public ResponseEntity<List<DynamoItem>> list() {
        return ResponseEntity.ok(dynamoDbService.list());
    }

    @PostMapping
    public ResponseEntity<DynamoItem> create(@RequestBody DynamoItem item) {
        return ResponseEntity.ok(dynamoDbService.create(item));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DynamoItem> update(
            @PathVariable String id,
            @RequestBody UpdateItemRequest request
    ) {
        return ResponseEntity.ok(dynamoDbService.update(id, request.name()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        dynamoDbService.delete(id);
        return ResponseEntity.noContent().build();
    }

    public record UpdateItemRequest(String name) {
    }
}
