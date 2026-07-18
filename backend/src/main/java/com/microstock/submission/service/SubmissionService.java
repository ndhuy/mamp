package com.microstock.submission.service;

import com.microstock.common.domain.SubmissionStatus;
import com.microstock.common.error.ApiException;
import com.microstock.common.security.OwnershipGuard;
import com.microstock.masterdata.domain.RejectionCategory;
import com.microstock.masterdata.domain.SiteCategory;
import com.microstock.masterdata.domain.StockSite;
import com.microstock.masterdata.repository.RejectionCategoryRepository;
import com.microstock.masterdata.repository.SiteCategoryRepository;
import com.microstock.masterdata.repository.StockSiteRepository;
import com.microstock.media.domain.MediaAsset;
import com.microstock.media.repository.MediaAssetRepository;
import com.microstock.submission.domain.SubmissionRecord;
import com.microstock.submission.repository.SubmissionRecordRepository;
import com.microstock.submission.web.dto.AddTargetSiteRequest;
import com.microstock.submission.web.dto.SubmissionResponse;
import com.microstock.submission.web.dto.SubmissionUpdateRequest;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SubmissionService {

    private final SubmissionRecordRepository submissionRepository;
    private final MediaAssetRepository mediaRepository;
    private final StockSiteRepository stockSiteRepository;
    private final SiteCategoryRepository siteCategoryRepository;
    private final RejectionCategoryRepository rejectionCategoryRepository;
    private final OwnershipGuard ownershipGuard;

    public SubmissionService(
            SubmissionRecordRepository submissionRepository,
            MediaAssetRepository mediaRepository,
            StockSiteRepository stockSiteRepository,
            SiteCategoryRepository siteCategoryRepository,
            RejectionCategoryRepository rejectionCategoryRepository,
            OwnershipGuard ownershipGuard) {
        this.submissionRepository = submissionRepository;
        this.mediaRepository = mediaRepository;
        this.stockSiteRepository = stockSiteRepository;
        this.siteCategoryRepository = siteCategoryRepository;
        this.rejectionCategoryRepository = rejectionCategoryRepository;
        this.ownershipGuard = ownershipGuard;
    }

    @Transactional(readOnly = true)
    public List<SubmissionResponse> listForMedia(UUID mediaId) {
        loadOwnedMedia(mediaId);
        return submissionRepository
                .findByMediaIdOrderByStockSite_DisplayOrderAscStockSite_NameAsc(mediaId)
                .stream().map(SubmissionResponse::from).toList();
    }

    /** Selecting a target site creates one Not Submitted record (AC-SUB-001). */
    @Transactional
    public SubmissionResponse addTargetSite(UUID mediaId, AddTargetSiteRequest req) {
        MediaAsset media = loadOwnedMedia(mediaId);
        if (media.isDeleted()) {
            throw ApiException.badRequest("Cannot add a target site to deleted media");
        }
        StockSite site = stockSiteRepository.findById(req.stockSiteId())
                .orElseThrow(() -> ApiException.badRequest("Stock site not found"));
        if (!site.isActive()) {
            throw ApiException.badRequest("Stock site is inactive");
        }
        if (submissionRepository.existsByMediaIdAndStockSiteId(mediaId, site.getId())) {
            throw ApiException.conflict("This stock site is already a target for the media"); // BR-009, AC-SUB-002
        }
        SubmissionRecord record = new SubmissionRecord(media, site);
        return SubmissionResponse.from(submissionRepository.save(record));
    }

    @Transactional
    public SubmissionResponse update(UUID submissionId, SubmissionUpdateRequest req) {
        SubmissionRecord record = loadOwnedSubmission(submissionId);
        StockSite site = record.getStockSite();

        applyCategories(record, site, req);
        applyStatusAndRejection(record, req);
        applyDates(record, req);

        record.setContributorAssetId(req.contributorAssetId());
        record.setAssetUrl(req.assetUrl());
        record.setNotes(req.notes());
        return SubmissionResponse.from(record);
    }

    @Transactional
    public void delete(UUID submissionId) {
        SubmissionRecord record = loadOwnedSubmission(submissionId);
        submissionRepository.delete(record);
    }

    // ----------------------------------------------------------------------
    // Validation helpers
    // ----------------------------------------------------------------------

    /** Category count + site-membership rules (PRD 6.10/6.12, VAL-012). */
    private void applyCategories(SubmissionRecord record, StockSite site, SubmissionUpdateRequest req) {
        int required = site.getCategoriesRequired();

        if (required < 2 && req.secondaryCategoryId() != null) {
            throw ApiException.badRequest("This stock site does not support a secondary category");
        }
        if (required >= 1 && req.primaryCategoryId() == null) {
            throw ApiException.badRequest("A primary category is required for this stock site");
        }
        if (required == 2 && req.secondaryCategoryId() == null) {
            throw ApiException.badRequest("A secondary category is required for this stock site");
        }
        if (req.primaryCategoryId() != null
                && Objects.equals(req.primaryCategoryId(), req.secondaryCategoryId())) {
            throw ApiException.badRequest("Primary and secondary categories must differ");
        }

        record.setPrimaryCategory(resolveCategory(req.primaryCategoryId(), site, record.getPrimaryCategory()));
        record.setSecondaryCategory(resolveCategory(req.secondaryCategoryId(), site, record.getSecondaryCategory()));
    }

    /**
     * Category must belong to the site (VAL-012) and be active — unless it is the
     * one already stored (inactive categories remain valid on historical records, PRD 6.10).
     */
    private SiteCategory resolveCategory(UUID categoryId, StockSite site, SiteCategory current) {
        if (categoryId == null) {
            return null;
        }
        SiteCategory category = siteCategoryRepository.findById(categoryId)
                .orElseThrow(() -> ApiException.badRequest("Site category not found"));
        if (!category.getStockSite().getId().equals(site.getId())) {
            throw ApiException.badRequest("Category does not belong to the selected stock site");
        }
        boolean unchanged = current != null && current.getId().equals(categoryId);
        if (!category.isActive() && !unchanged) {
            throw ApiException.badRequest("Category is inactive and cannot be selected");
        }
        return category;
    }

    /** Rejected requires category + detail; other statuses clear rejection fields (BR-010). */
    private void applyStatusAndRejection(SubmissionRecord record, SubmissionUpdateRequest req) {
        record.setStatus(req.status());
        if (req.status() == SubmissionStatus.REJECTED) {
            if (req.rejectionCategoryId() == null
                    || req.rejectionDetail() == null || req.rejectionDetail().isBlank()) {
                throw ApiException.badRequest("Rejected submissions require a rejection category and detail");
            }
            RejectionCategory category = rejectionCategoryRepository.findById(req.rejectionCategoryId())
                    .orElseThrow(() -> ApiException.badRequest("Rejection category not found"));
            record.setRejectionCategory(category);
            record.setRejectionDetail(req.rejectionDetail().trim());
        } else {
            record.setRejectionCategory(null);
            record.setRejectionDetail(null);
        }
    }

    /** Reviewed date cannot precede submitted date (VAL-014, BR-011). */
    private void applyDates(SubmissionRecord record, SubmissionUpdateRequest req) {
        if (req.submittedDate() != null && req.reviewedDate() != null
                && req.reviewedDate().isBefore(req.submittedDate())) {
            throw ApiException.badRequest("Reviewed date cannot be earlier than submitted date");
        }
        record.setSubmittedDate(req.submittedDate());
        record.setReviewedDate(req.reviewedDate());
    }

    // ----------------------------------------------------------------------
    // Ownership-aware loaders
    // ----------------------------------------------------------------------

    private MediaAsset loadOwnedMedia(UUID mediaId) {
        MediaAsset media = mediaRepository.findById(mediaId)
                .orElseThrow(() -> ApiException.notFound("Media not found"));
        ownershipGuard.assertCanAccess(media);
        return media;
    }

    private SubmissionRecord loadOwnedSubmission(UUID submissionId) {
        SubmissionRecord record = submissionRepository.findWithDetailsById(submissionId)
                .orElseThrow(() -> ApiException.notFound("Submission not found"));
        ownershipGuard.assertCanAccess(record.getMedia()); // inherits media ownership
        return record;
    }
}
