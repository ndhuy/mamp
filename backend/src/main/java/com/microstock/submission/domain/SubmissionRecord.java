package com.microstock.submission.domain;

import com.microstock.common.domain.SubmissionStatus;
import com.microstock.masterdata.domain.RejectionCategory;
import com.microstock.masterdata.domain.SiteCategory;
import com.microstock.masterdata.domain.StockSite;
import com.microstock.media.domain.MediaAsset;
import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/** Tracks one Media Asset at one Stock Site (PRD 6.12). Private through its media. */
@Entity
@Table(name = "submission_record")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SubmissionRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "media_id", nullable = false, updatable = false)
    private MediaAsset media;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "stock_site_id", nullable = false, updatable = false)
    private StockSite stockSite;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SubmissionStatus status = SubmissionStatus.NOT_SUBMITTED;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "primary_category_id")
    private SiteCategory primaryCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "secondary_category_id")
    private SiteCategory secondaryCategory;

    @Column(name = "contributor_asset_id")
    private String contributorAssetId;

    @Column(name = "asset_url", columnDefinition = "text")
    private String assetUrl;

    @Column(name = "submitted_date")
    private LocalDate submittedDate;

    @Column(name = "reviewed_date")
    private LocalDate reviewedDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rejection_category_id")
    private RejectionCategory rejectionCategory;

    @Column(name = "rejection_detail", columnDefinition = "text")
    private String rejectionDetail;

    @Column(columnDefinition = "text")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public SubmissionRecord(MediaAsset media, StockSite stockSite) {
        this.media = media;
        this.stockSite = stockSite;
    }
}
