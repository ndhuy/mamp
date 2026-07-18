package com.microstock.submission.web.dto;

import com.microstock.common.domain.SubmissionStatus;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

/** Full update of a submission record (PRD 6.12). Stock site is immutable. */
public record SubmissionUpdateRequest(
        @NotNull SubmissionStatus status,
        UUID primaryCategoryId,
        UUID secondaryCategoryId,
        String contributorAssetId,
        String assetUrl,
        LocalDate submittedDate,
        LocalDate reviewedDate,
        UUID rejectionCategoryId,
        String rejectionDetail,
        String notes) {
}
