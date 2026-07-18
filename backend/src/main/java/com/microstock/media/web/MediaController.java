package com.microstock.media.web;

import com.microstock.media.service.MediaService;
import com.microstock.media.web.dto.MediaFilter;
import com.microstock.media.web.dto.MediaRequest;
import com.microstock.media.web.dto.MediaResponse;
import com.microstock.media.web.dto.MediaSummaryResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/media")
public class MediaController {

    private final MediaService mediaService;

    public MediaController(MediaService mediaService) {
        this.mediaService = mediaService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MediaResponse create(@Valid @RequestBody MediaRequest request) {
        return mediaService.create(request);
    }

    @GetMapping("/{id}")
    public MediaResponse get(@PathVariable UUID id) {
        return mediaService.get(id);
    }

    @PutMapping("/{id}")
    public MediaResponse update(@PathVariable UUID id, @Valid @RequestBody MediaRequest request) {
        return mediaService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        mediaService.softDelete(id);
    }

    @PostMapping("/{id}/restore")
    public MediaResponse restore(@PathVariable UUID id) {
        return mediaService.restore(id);
    }

    @GetMapping
    public Page<MediaSummaryResponse> list(
            MediaFilter filter,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return mediaService.search(filter, pageable);
    }

    @GetMapping("/deleted")
    public Page<MediaSummaryResponse> listDeleted(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return mediaService.listDeleted(pageable);
    }
}
