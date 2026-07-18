package com.microstock.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Binds app.storage.* settings for the S3-compatible object store (MinIO in dev). */
@ConfigurationProperties(prefix = "app.storage")
public record AppStorageProperties(
        String endpoint,
        String accessKey,
        String secretKey,
        String bucket,
        String region,
        String publicBaseUrl) {
}
