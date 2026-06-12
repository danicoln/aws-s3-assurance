package com.danicoln.awss3assurance.controller;

import com.danicoln.awss3assurance.service.S3Service;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;

import java.io.IOException;

@RestController
@RequestMapping("/s3")
public class S3Controller {

    private final S3Service service;

    public S3Controller(S3Service service) {
        this.service = service;
    }

    @GetMapping("/list")
    public ResponseEntity<S3Service.ListResult> list(
            @RequestParam(value = "prefix", required = false) String prefix
    ) {
        return ResponseEntity.ok(service.list(prefix));
    }

    @PostMapping("/upload")
    public ResponseEntity<String> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "prefix", required = false) String prefix
    ) throws IOException {
        service.upload(file, prefix);
        return ResponseEntity.ok("Upload completed successfully");
    }

    @GetMapping("/file")
    public ResponseEntity<InputStreamResource> getFile(@RequestParam("key") String key) {
        ResponseInputStream<GetObjectResponse> stream = service.getFile(key);
        GetObjectResponse metadata = stream.response();

        String filename = key.contains("/")
                ? key.substring(key.lastIndexOf('/') + 1)
                : key;

        String contentType = metadata.contentType() != null
                ? metadata.contentType()
                : MediaType.APPLICATION_OCTET_STREAM_VALUE;

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(new InputStreamResource(stream));
    }

    @GetMapping("/metadata")
    public ResponseEntity<MetadataResponse> getMetadata(@RequestParam("key") String key) {
        HeadObjectResponse metadata = service.getMetadata(key);
        return ResponseEntity.ok(new MetadataResponse(
                key,
                metadata.contentLength(),
                metadata.contentType(),
                metadata.lastModified() != null ? metadata.lastModified().toString() : null,
                metadata.eTag()
        ));
    }

    public record MetadataResponse(
            String key,
            Long sizeBytes,
            String contentType,
            String lastModified,
            String eTag
    ) {
    }
}
