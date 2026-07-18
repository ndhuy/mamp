package com.microstock.submission.web.dto;

import com.microstock.common.domain.SubmissionStatus;
import com.microstock.submission.domain.SubmissionRecord;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record SubmissionResponse(
        UUID id,
        UUID mediaId,
        UUID stockSiteId,
        String stockSiteName,
        SubmissionStatus status,
        Ref primaryCategory,
        Ref secondaryCategory,
        String contributorAssetId,
        String assetUrl,
        LocalDate submittedDate,
        LocalDate reviewedDate,
        Ref rejectionCategory,
        String rejectionDetail,
        String notes,
        Instant createdAt,
        Instant updatedAt) {

    public record Ref(UUID id, String name) {}

    public static SubmissionResponse from(SubmissionRecord s) {
        return new SubmissionResponse(
                s.getId(),
                s.getMedia().getId(),
                s.getStockSite().getId(),
                s.getStockSite().getName(),
                s.getStatus(),
                ref(s.getPrimaryCategory() == null ? null : s.getPrimaryCategory().getId(),
                        s.getPrimaryCategory() == null ? null : s.getPrimaryCategory().getName()),
                ref(s.getSecondaryCategory() == null ? null : s.getSecondaryCategory().getId(),
                        s.getSecondaryCategory() == null ? null : s.getSecondaryCategory().getName()),
                s.getContributorAssetId(),
                s.getAssetUrl(),
                s.getSubmittedDate(),
                s.getReviewedDate(),
                ref(s.getRejectionCategory() == null ? null : s.getRejectionCategory().getId(),
                        s.getRejectionCategory() == null ? null : s.getRejectionCategory().getName()),
                s.getRejectionDetail(),
                s.getNotes(),
                s.getCreatedAt(),
                s.getUpdatedAt());
    }

    private static Ref ref(UUID id, String name) {
        return id == null ? null : new Ref(id, name);
    }
}
