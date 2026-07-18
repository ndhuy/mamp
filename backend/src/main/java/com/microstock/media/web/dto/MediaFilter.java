package com.microstock.media.web.dto;

import com.microstock.common.domain.ContentUsageType;
import com.microstock.common.domain.MediaType;
import com.microstock.common.domain.SubmissionStatus;
import com.microstock.common.domain.WorkflowStatus;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Bound from query parameters on GET /api/media (PRD 7.1-7.3).
 * All fields are optional; null means "no constraint".
 */
public record MediaFilter(
        String q,                          // free-text search
        MediaType mediaType,
        WorkflowStatus workflowStatus,
        ContentUsageType contentUsageType,
        UUID captureDeviceId,
        UUID lensId,
        UUID conceptId,
        String keyword,                    // exact normalized keyword
        UUID stockSiteId,
        SubmissionStatus submissionStatus,
        Boolean hasThumbnail,
        Boolean hasKeywords,
        Boolean hasTargetSites,
        LocalDate captureDateFrom,
        LocalDate captureDateTo,
        String smart,                      // smart-filter key (see MediaSpecifications)
        Boolean deleted,                   // true = deleted only; else active only
        UUID owner) {                      // admin-only owner narrowing
}
