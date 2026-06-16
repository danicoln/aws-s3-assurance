package com.danicoln.awss3assurance.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(ResourceNotFoundException exception) {
        return build(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(ResourceConflictException.class)
    public ResponseEntity<ApiErrorResponse> handleConflict(ResourceConflictException exception) {
        return build(HttpStatus.CONFLICT, exception.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(IllegalArgumentException exception) {
        return build(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(NoSuchKeyException.class)
    public ResponseEntity<ApiErrorResponse> handleNoSuchKey(NoSuchKeyException exception) {
        return build(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(S3Exception.class)
    public ResponseEntity<ApiErrorResponse> handleS3Exception(S3Exception exception) {
        return build(HttpStatus.BAD_GATEWAY, exception.awsErrorDetails() != null
                ? exception.awsErrorDetails().errorMessage()
                : exception.getMessage());
    }

    @ExceptionHandler(DynamoDbException.class)
    public ResponseEntity<ApiErrorResponse> handleDynamoDbException(DynamoDbException exception) {
        return build(HttpStatus.BAD_GATEWAY, exception.awsErrorDetails() != null
                ? exception.awsErrorDetails().errorMessage()
                : exception.getMessage());
    }

    @ExceptionHandler(SdkClientException.class)
    public ResponseEntity<ApiErrorResponse> handleSdkClientException(SdkClientException exception) {
        return build(HttpStatus.SERVICE_UNAVAILABLE, exception.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalState(IllegalStateException exception) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
    }

    private ResponseEntity<ApiErrorResponse> build(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(new ApiErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message
        ));
    }
}
