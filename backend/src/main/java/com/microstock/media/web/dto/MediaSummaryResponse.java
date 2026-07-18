package com.microstock.media.web.dto;

import com.microstock.common.domain.ContentUsageType;
import com.microstock.common.domain.MediaType;
import com.microstock.common.domain.WorkflowStatus;
import com.microstock.media.domain.MediaAsset;
import java.time.Instant;
import java.util.UUID;

/** Compact projection for table/grid list views (PRD 7.4). */
public record MediaSummaryResponse(
        UUID id,
        String code,
        UUID ownerId,
        String title,
        MediaType mediaType,
        String thumbnailKey,
        WorkflowStatus workflowStatus,
        ContentUsageType contentUsageType,
        int conceptCount,
        int keywordCount,
        Instant createdAt,
        Instant updatedAt) {

    public static MediaSummaryResponse from(MediaAsset m) {
        return new MediaSummaryResponse(
                m.getId(), m.getCode(), m.getOwnerId(), m.getTitle(), m.getMediaType(),
                m.getThumbnailKey(), m.getWorkflowStatus(), m.getContentUsageType(),
                m.getConcepts().size(), m.getKeywords().size(),
                m.getCreatedAt(), m.getUpdatedAt());
    }
}
