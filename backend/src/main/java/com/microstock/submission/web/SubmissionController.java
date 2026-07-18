package com.microstock.submission.web;

import com.microstock.submission.service.SubmissionService;
import com.microstock.submission.web.dto.AddTargetSiteRequest;
import com.microstock.submission.web.dto.SubmissionResponse;
import com.microstock.submission.web.dto.SubmissionUpdateRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
public class SubmissionController {

    private final SubmissionService service;

    public SubmissionController(SubmissionService service) {
        this.service = service;
    }

    /** List submissions (target sites) for a media asset. */
    @GetMapping("/api/media/{mediaId}/submissions")
    public List<SubmissionResponse> list(@PathVariable UUID mediaId) {
        return service.listForMedia(mediaId);
    }

    /** Add a target site → creates a Not Submitted record. */
    @PostMapping("/api/media/{mediaId}/submissions")
    @ResponseStatus(HttpStatus.CREATED)
    public SubmissionResponse addTargetSite(
            @PathVariable UUID mediaId, @Valid @RequestBody AddTargetSiteRequest request) {
        return service.addTargetSite(mediaId, request);
    }

    @PutMapping("/api/submissions/{id}")
    public SubmissionResponse update(@PathVariable UUID id, @Valid @RequestBody SubmissionUpdateRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/api/submissions/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }
}
