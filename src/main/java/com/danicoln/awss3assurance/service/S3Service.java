package com.danicoln.awss3assurance.service;

import com.danicoln.awss3assurance.properties.AwsStorageProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CommonPrefix;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class S3Service {

    private final S3Client s3Client;
    private final AwsStorageProperties properties;

    public S3Service(S3Client s3Client, AwsStorageProperties properties) {
        this.s3Client = s3Client;
        this.properties = properties;
    }

    public void upload(MultipartFile file, String prefix) throws IOException {
        String key = (prefix == null || prefix.isBlank())
                ? file.getOriginalFilename()
                : normalizePrefix(prefix) + file.getOriginalFilename();

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket())
                .key(key)
                .contentType(file.getContentType())
                .build();

        s3Client.putObject(request, RequestBody.fromBytes(file.getBytes()));
    }

    public ListResult list(String prefix) {
        String normalized = (prefix == null || prefix.isBlank())
                ? ""
                : normalizePrefix(prefix);

        ListObjectsV2Request request = ListObjectsV2Request.builder()
                .bucket(bucket())
                .prefix(normalized)
                .delimiter("/")
                .build();

        ListObjectsV2Response response = s3Client.listObjectsV2(request);

        List<String> folders = response.commonPrefixes()
                .stream()
                .map(CommonPrefix::prefix)
                .toList();

        List<String> files = new ArrayList<>();
        for (S3Object object : response.contents()) {
            if (!object.key().equals(normalized)) {
                files.add(object.key());
            }
        }

        return new ListResult(folders, files);
    }

    public ResponseInputStream<GetObjectResponse> getFile(String key) {
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucket())
                .key(key)
                .build();

        return s3Client.getObject(request);
    }

    public HeadObjectResponse getMetadata(String key) {
        HeadObjectRequest request = HeadObjectRequest.builder()
                .bucket(bucket())
                .key(key)
                .build();

        return s3Client.headObject(request);
    }

    private String bucket() {
        String bucket = properties.getS3().getBucket();
        if (bucket == null || bucket.isBlank()) {
            throw new IllegalStateException("Missing configuration for app.aws.s3.bucket or AWS_S3_BUCKET");
        }
        return bucket;
    }

    private String normalizePrefix(String prefix) {
        return prefix.endsWith("/") ? prefix : prefix + "/";
    }

    public record ListResult(List<String> folders, List<String> files) {
    }
}
