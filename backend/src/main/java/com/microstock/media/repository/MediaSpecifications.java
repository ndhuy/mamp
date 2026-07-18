package com.microstock.media.repository;

import com.microstock.common.domain.SubmissionStatus;
import com.microstock.common.util.Normalizer;
import com.microstock.concept.domain.Concept;
import com.microstock.keyword.domain.Keyword;
import com.microstock.media.domain.MediaAsset;
import com.microstock.media.web.dto.MediaFilter;
import com.microstock.submission.domain.SubmissionRecord;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

/** Builds a dynamic query for the media list (search, filters, smart filters). */
public final class MediaSpecifications {

    /** Submission statuses that count as "actually submitted somewhere". */
    private static final List<SubmissionStatus> SUBMITTED_STATUSES = List.of(
            SubmissionStatus.SUBMITTED, SubmissionStatus.IN_REVIEW, SubmissionStatus.ACCEPTED,
            SubmissionStatus.REJECTED, SubmissionStatus.RESUBMIT_REQUIRED, SubmissionStatus.RESUBMITTED);

    private MediaSpecifications() {}

    public static Specification<MediaAsset> build(MediaFilter f, UUID currentUserId, boolean isAdmin) {
        return (root, query, cb) -> {
            List<Predicate> ps = new ArrayList<>();

            // --- Ownership (BR-002/003) ---
            if (isAdmin) {
                if (f.owner() != null) ps.add(cb.equal(root.get("ownerId"), f.owner()));
            } else {
                ps.add(cb.equal(root.get("ownerId"), currentUserId));
            }

            // --- Active vs deleted ---
            ps.add(Boolean.TRUE.equals(f.deleted())
                    ? cb.isNotNull(root.get("deletedAt"))
                    : cb.isNull(root.get("deletedAt")));

            // --- Simple equality filters ---
            if (f.mediaType() != null) ps.add(cb.equal(root.get("mediaType"), f.mediaType()));
            if (f.workflowStatus() != null) ps.add(cb.equal(root.get("workflowStatus"), f.workflowStatus()));
            if (f.contentUsageType() != null) ps.add(cb.equal(root.get("contentUsageType"), f.contentUsageType()));
            if (f.captureDeviceId() != null) ps.add(cb.equal(root.get("captureDevice").get("id"), f.captureDeviceId()));
            if (f.lensId() != null) ps.add(cb.equal(root.get("lens").get("id"), f.lensId()));

            // --- Capture-date range ---
            if (f.captureDateFrom() != null) ps.add(cb.greaterThanOrEqualTo(root.get("captureDate"), f.captureDateFrom()));
            if (f.captureDateTo() != null) ps.add(cb.lessThanOrEqualTo(root.get("captureDate"), f.captureDateTo()));

            // --- Thumbnail presence ---
            if (f.hasThumbnail() != null) {
                ps.add(f.hasThumbnail() ? cb.isNotNull(root.get("thumbnailKey")) : cb.isNull(root.get("thumbnailKey")));
            }

            // --- Free-text search (case-insensitive) across media fields + keywords ---
            if (hasText(f.q())) {
                String like = "%" + f.q().trim().toLowerCase() + "%";
                ps.add(cb.or(
                        like(cb, root.get("code"), like),
                        like(cb, root.get("title"), like),
                        like(cb, root.get("description"), like),
                        like(cb, root.get("originalFilePath"), like),
                        like(cb, root.get("exportFilePath"), like),
                        cb.exists(keywordSubquery(query, cb, root, kj -> like(cb, kj.get("value"), like)))));
            }

            // --- Exact keyword filter (normalized) ---
            if (hasText(f.keyword())) {
                String norm = Normalizer.normalize(f.keyword());
                ps.add(cb.exists(keywordSubquery(query, cb, root,
                        kj -> cb.equal(kj.get("normalizedValue"), norm))));
            }

            // --- Concept ---
            if (f.conceptId() != null) {
                ps.add(cb.exists(conceptSubquery(query, cb, root, f.conceptId())));
            }

            // --- Has/does not have keywords ---
            if (f.hasKeywords() != null) {
                Predicate exists = cb.exists(keywordSubquery(query, cb, root, kj -> cb.conjunction()));
                ps.add(f.hasKeywords() ? exists : cb.not(exists));
            }

            // --- Submission / target-site filters ---
            if (f.stockSiteId() != null || f.submissionStatus() != null) {
                ps.add(cb.exists(submissionSubquery(query, cb, root, f.stockSiteId(), f.submissionStatus())));
            }
            if (f.hasTargetSites() != null) {
                Predicate exists = cb.exists(submissionSubquery(query, cb, root, null, null));
                ps.add(f.hasTargetSites() ? exists : cb.not(exists));
            }

            // --- Smart filters ---
            applySmart(f.smart(), root, query, cb, ps);

            return cb.and(ps.toArray(new Predicate[0]));
        };
    }

    private static void applySmart(
            String smart, Root<MediaAsset> root, CriteriaQuery<?> query, CriteriaBuilder cb, List<Predicate> ps) {
        if (!hasText(smart)) return;
        switch (smart) {
            case "READY" -> ps.add(cb.equal(root.get("workflowStatus"), com.microstock.common.domain.WorkflowStatus.READY));
            case "MISSING_THUMBNAIL" -> ps.add(cb.isNull(root.get("thumbnailKey")));
            case "MISSING_KEYWORDS" ->
                    ps.add(cb.not(cb.exists(keywordSubquery(query, cb, root, kj -> cb.conjunction()))));
            case "NO_TARGET_SITE" ->
                    ps.add(cb.not(cb.exists(submissionSubquery(query, cb, root, null, null))));
            case "NOT_SUBMITTED_ANY" ->
                    ps.add(cb.not(cb.exists(submissionStatusSubquery(query, cb, root, SUBMITTED_STATUSES))));
            case "IN_REVIEW" ->
                    ps.add(cb.exists(submissionSubquery(query, cb, root, null, SubmissionStatus.IN_REVIEW)));
            case "ACCEPTED_ANY" ->
                    ps.add(cb.exists(submissionSubquery(query, cb, root, null, SubmissionStatus.ACCEPTED)));
            case "REJECTED_ANY" ->
                    ps.add(cb.exists(submissionSubquery(query, cb, root, null, SubmissionStatus.REJECTED)));
            default -> { /* unknown smart key: ignore */ }
        }
    }

    // --- Subquery builders ---

    private interface KeywordPredicate {
        Predicate apply(Join<MediaAsset, Keyword> keywordJoin);
    }

    private static Subquery<Integer> keywordSubquery(
            CriteriaQuery<?> query, CriteriaBuilder cb, Root<MediaAsset> root, KeywordPredicate extra) {
        Subquery<Integer> sq = query.subquery(Integer.class);
        Root<MediaAsset> sub = sq.from(MediaAsset.class);
        Join<MediaAsset, Keyword> kj = sub.join("keywords");
        sq.select(cb.literal(1));
        sq.where(cb.equal(sub.get("id"), root.get("id")), extra.apply(kj));
        return sq;
    }

    private static Subquery<Integer> conceptSubquery(
            CriteriaQuery<?> query, CriteriaBuilder cb, Root<MediaAsset> root, UUID conceptId) {
        Subquery<Integer> sq = query.subquery(Integer.class);
        Root<MediaAsset> sub = sq.from(MediaAsset.class);
        Join<MediaAsset, Concept> cj = sub.join("concepts");
        sq.select(cb.literal(1));
        sq.where(cb.equal(sub.get("id"), root.get("id")), cb.equal(cj.get("id"), conceptId));
        return sq;
    }

    private static Subquery<Integer> submissionSubquery(
            CriteriaQuery<?> query, CriteriaBuilder cb, Root<MediaAsset> root, UUID stockSiteId, SubmissionStatus status) {
        Subquery<Integer> sq = query.subquery(Integer.class);
        Root<SubmissionRecord> sub = sq.from(SubmissionRecord.class);
        sq.select(cb.literal(1));
        List<Predicate> where = new ArrayList<>();
        where.add(cb.equal(sub.get("media").get("id"), root.get("id")));
        if (stockSiteId != null) where.add(cb.equal(sub.get("stockSite").get("id"), stockSiteId));
        if (status != null) where.add(cb.equal(sub.get("status"), status));
        sq.where(where.toArray(new Predicate[0]));
        return sq;
    }

    private static Subquery<Integer> submissionStatusSubquery(
            CriteriaQuery<?> query, CriteriaBuilder cb, Root<MediaAsset> root, List<SubmissionStatus> statuses) {
        Subquery<Integer> sq = query.subquery(Integer.class);
        Root<SubmissionRecord> sub = sq.from(SubmissionRecord.class);
        sq.select(cb.literal(1));
        sq.where(cb.equal(sub.get("media").get("id"), root.get("id")), sub.get("status").in(statuses));
        return sq;
    }

    private static Predicate like(CriteriaBuilder cb, jakarta.persistence.criteria.Expression<String> path, String like) {
        return cb.like(cb.lower(path), like);
    }

    private static boolean hasText(String s) {
        return s != null && !s.isBlank();
    }
}
