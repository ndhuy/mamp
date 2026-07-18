package com.microstock.config;

import java.net.URI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

/** Builds an S3 client pointed at the configured endpoint (MinIO for local dev). */
@Configuration
public class S3Config {

    @Bean
    public S3Client s3Client(AppStorageProperties props) {
        return S3Client.builder()
                .endpointOverride(URI.create(props.endpoint()))
                .region(Region.of(props.region()))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(props.accessKey(), props.secretKey())))
                .serviceConfiguration(S3Configuration.builder()
                        // Path-style access is required for MinIO (no virtual-hosted buckets).
                        .pathStyleAccessEnabled(true)
                        // Disable aws-chunked streaming signatures, which MinIO resets.
                        .chunkedEncodingEnabled(false)
                        .build())
                .build();
    }
}
