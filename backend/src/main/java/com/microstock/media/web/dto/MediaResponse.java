package com.microstock.media.web.dto;

import com.microstock.common.domain.ContentUsageType;
import com.microstock.common.domain.MediaType;
import com.microstock.common.domain.StorageType;
import com.microstock.common.domain.WorkflowStatus;
import com.microstock.media.domain.MediaAsset;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Full media detail response. */
public record MediaResponse(
        UUID id,
        String code,
        UUID ownerId,
        String title,
        MediaType mediaType,
        String thumbnailKey,
        String description,
        String notes,
        LocalDate captureDate,
        String location,
        NamedRef captureDevice,
        NamedRef lens,
        ContentUsageType contentUsageType,
        WorkflowStatus workflowStatus,
        String originalFilePath,
        String exportFilePath,
        StorageType storageType,
        boolean aiGenerated,
        String editorialCaption,
        LocalDate eventDate,
        String editorialLocation,
        String eventSubjectName,
        String editorialNotes,
        List<NamedRef> concepts,
        List<String> keywords,
        boolean deleted,
        Instant createdAt,
        Instant updatedAt) {

    public record NamedRef(UUID id, String name) {}

    public static MediaResponse from(MediaAsset m) {
        NamedRef device = m.getCaptureDevice() == null ? null
                : new NamedRef(m.getCaptureDevice().getId(),
                        m.getCaptureDevice().getBrand() + " " + m.getCaptureDevice().getModel());
        NamedRef lens = m.getLens() == null ? null
                : new NamedRef(m.getLens().getId(),
                        m.getLens().getBrand() + " " + m.getLens().getModel());
        List<NamedRef> concepts = m.getConcepts().stream()
                .map(c -> new NamedRef(c.getId(), c.getName()))
                .toList();
        List<String> keywords = m.getKeywords().stream()
                .map(k -> k.getValue())
                .toList();
        return new MediaResponse(
                m.getId(), m.getCode(), m.getOwnerId(), m.getTitle(), m.getMediaType(),
                m.getThumbnailKey(), m.getDescription(), m.getNotes(), m.getCaptureDate(), m.getLocation(),
                device, lens, m.getContentUsageType(), m.getWorkflowStatus(),
                m.getOriginalFilePath(), m.getExportFilePath(), m.getStorageType(), m.isAiGenerated(),
                m.getEditorialCaption(), m.getEventDate(), m.getEditorialLocation(),
                m.getEventSubjectName(), m.getEditorialNotes(),
                concepts, keywords, m.isDeleted(), m.getCreatedAt(), m.getUpdatedAt());
    }
}
