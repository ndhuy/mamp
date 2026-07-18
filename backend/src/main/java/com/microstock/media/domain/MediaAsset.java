package com.microstock.media.domain;

import com.microstock.common.domain.*;
import com.microstock.concept.domain.Concept;
import com.microstock.keyword.domain.Keyword;
import com.microstock.masterdata.domain.CaptureDevice;
import com.microstock.masterdata.domain.Lens;
import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.generator.EventType;

/** Central Photo or Footage record owned by one User (PRD 6.1). */
@Entity
@Table(name = "media_asset")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MediaAsset implements OwnedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    /**
     * Immutable business code (MED-000001), minted by a DB sequence default.
     * {@code @Generated(INSERT)} makes it non-insertable and triggers a post-insert
     * SELECT so the value is available on the entity immediately after save.
     */
    @Generated(event = EventType.INSERT)
    @Column(name = "code", updatable = false)
    private String code;

    @Column(name = "owner_id", nullable = false, updatable = false)
    private UUID ownerId;

    @Column(nullable = false, length = 120)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "media_type", nullable = false, length = 10)
    private MediaType mediaType;

    @Column(name = "thumbnail_key", length = 512)
    private String thumbnailKey;

    @Column(columnDefinition = "text")
    private String description;

    @Column(columnDefinition = "text")
    private String notes;

    @Column(name = "capture_date")
    private LocalDate captureDate;

    private String location;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "capture_device_id")
    private CaptureDevice captureDevice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lens_id")
    private Lens lens;

    @Enumerated(EnumType.STRING)
    @Column(name = "content_usage_type", length = 12)
    private ContentUsageType contentUsageType;

    @Enumerated(EnumType.STRING)
    @Column(name = "workflow_status", nullable = false, length = 20)
    private WorkflowStatus workflowStatus = WorkflowStatus.DRAFT;

    @Column(name = "original_file_path", columnDefinition = "text")
    private String originalFilePath;

    @Column(name = "export_file_path", columnDefinition = "text")
    private String exportFilePath;

    @Enumerated(EnumType.STRING)
    @Column(name = "storage_type", length = 20)
    private StorageType storageType;

    @Column(name = "is_ai_generated", nullable = false)
    private boolean aiGenerated = false;

    // Editorial-only fields (PRD 6.8)
    @Column(name = "editorial_caption", columnDefinition = "text")
    private String editorialCaption;

    @Column(name = "event_date")
    private LocalDate eventDate;

    @Column(name = "editorial_location")
    private String editorialLocation;

    @Column(name = "event_subject_name")
    private String eventSubjectName;

    @Column(name = "editorial_notes", columnDefinition = "text")
    private String editorialNotes;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "media_concept",
            joinColumns = @JoinColumn(name = "media_id"),
            inverseJoinColumns = @JoinColumn(name = "concept_id"))
    private Set<Concept> concepts = new LinkedHashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "media_keyword",
            joinColumns = @JoinColumn(name = "media_id"),
            inverseJoinColumns = @JoinColumn(name = "keyword_id"))
    private Set<Keyword> keywords = new LinkedHashSet<>();

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public MediaAsset(UUID ownerId, String title, MediaType mediaType) {
        this.ownerId = ownerId;
        this.title = title;
        this.mediaType = mediaType;
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }
}
