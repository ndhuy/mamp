package com.microstock.storage.web;

import com.microstock.storage.StorageService;
import com.microstock.storage.web.dto.UploadResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class UploadController {

    private final StorageService storageService;

    public UploadController(StorageService storageService) {
        this.storageService = storageService;
    }

    /** Uploads a thumbnail image and returns its storage key + public URL. */
    @PostMapping(value = "/api/uploads/thumbnail", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UploadResponse uploadThumbnail(@RequestParam("file") MultipartFile file) {
        String key = storageService.uploadThumbnail(file);
        return new UploadResponse(key, storageService.publicUrl(key));
    }
}
