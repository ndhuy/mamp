package com.microstock.media.web.dto;

import com.microstock.common.domain.ContentUsageType;
import com.microstock.common.domain.MediaType;
import com.microstock.common.domain.StorageType;
import com.microstock.common.domain.WorkflowStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Create/update payload for a media asset (PRD 6.1). */
public record MediaRequest(
        @NotBlank @Size(max = 120) String title,
        @NotNull MediaType mediaType,
        String thumbnailKey,
        String description,
        String notes,
        LocalDate captureDate,
        String location,
        UUID captureDeviceId,
        UUID lensId,
        ContentUsageType contentUsageType,
        WorkflowStatus workflowStatus,
        String originalFilePath,
        String exportFilePath,
        StorageType storageType,
        Boolean aiGenerated,
        // Editorial-only
        String editorialCaption,
        LocalDate eventDate,
        String editorialLocation,
        String eventSubjectName,
        String editorialNotes,
        // Associations
        List<UUID> conceptIds,
        List<String> keywords) {
}
