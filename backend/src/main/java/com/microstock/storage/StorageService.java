package com.microstock.storage;

import com.microstock.common.error.ApiException;
import com.microstock.config.AppStorageProperties;
import java.io.IOException;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

/** Uploads assets to the S3-compatible object store and builds public URLs. */
@Service
public class StorageService {

    private static final Set<String> ALLOWED_IMAGE_TYPES =
            Set.of("image/jpeg", "image/png", "image/webp", "image/gif");
    private static final long MAX_THUMBNAIL_BYTES = 5L * 1024 * 1024; // 5 MB

    private final S3Client s3;
    private final AppStorageProperties props;

    public StorageService(S3Client s3, AppStorageProperties props) {
        this.s3 = s3;
        this.props = props;
    }

    /** Stores a thumbnail image and returns its object key. */
    public String uploadThumbnail(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw ApiException.badRequest("No file provided");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType)) {
            throw ApiException.badRequest("Thumbnail must be a JPEG, PNG, WEBP, or GIF image");
        }
        if (file.getSize() > MAX_THUMBNAIL_BYTES) {
            throw ApiException.badRequest("Thumbnail must be 5 MB or smaller");
        }

        String key = "thumbnails/" + UUID.randomUUID() + extensionFor(contentType);
        try {
            // Small images: read fully so the payload has a known length and no
            // streaming signature (avoids the MinIO "connection reset" on aws-chunked).
            byte[] bytes = file.getBytes();
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(props.bucket())
                    .key(key)
                    .contentType(contentType)
                    .build();
            s3.putObject(request, RequestBody.fromBytes(bytes));
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read upload", e);
        }
        return key;
    }

    public String publicUrl(String key) {
        return key == null ? null : props.publicBaseUrl() + "/" + key;
    }

    private String extensionFor(String contentType) {
        return switch (contentType) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            case "image/gif" -> ".gif";
            default -> "";
        };
    }
}
