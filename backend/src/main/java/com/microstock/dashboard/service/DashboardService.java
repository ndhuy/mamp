package com.microstock.dashboard.service;

import com.microstock.common.domain.MediaType;
import com.microstock.common.domain.SubmissionStatus;
import com.microstock.common.domain.WorkflowStatus;
import com.microstock.common.security.OwnershipGuard;
import com.microstock.common.security.SecurityUtils;
import com.microstock.dashboard.web.dto.DashboardResponse;
import com.microstock.dashboard.web.dto.DashboardResponse.Count;
import com.microstock.dashboard.web.dto.DashboardResponse.SiteReview;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Computes dashboard metrics. Standard users see only their own data; admins see
 * system-wide data (BR-018, BR-020). Deleted media is excluded from all metrics.
 */
@Service
public class DashboardService {

    private final EntityManager em;
    private final OwnershipGuard ownershipGuard;

    public DashboardService(EntityManager em, OwnershipGuard ownershipGuard) {
        this.em = em;
        this.ownershipGuard = ownershipGuard;
    }

    @Transactional(readOnly = true)
    public DashboardResponse compute() {
        boolean admin = ownershipGuard.isAdmin();
        UUID owner = admin ? null : SecurityUtils.currentUserId();

        long total = countMedia(owner, null);
        long photos = countMedia(owner, MediaType.PHOTO);
        long footage = countMedia(owner, MediaType.FOOTAGE);
        long ready = countMediaByStatus(owner, WorkflowStatus.READY);
        long missingKeywords = countMediaMissingKeywords(owner);

        Map<SubmissionStatus, Long> subByStatus = submissionsByStatus(owner);
        long totalSub = subByStatus.values().stream().mapToLong(Long::longValue).sum();
        long accepted = subByStatus.getOrDefault(SubmissionStatus.ACCEPTED, 0L);
        long rejected = subByStatus.getOrDefault(SubmissionStatus.REJECTED, 0L);
        long inReview = subByStatus.getOrDefault(SubmissionStatus.IN_REVIEW, 0L);
        long submitted = subByStatus.getOrDefault(SubmissionStatus.SUBMITTED, 0L);
        double acceptanceRate = (accepted + rejected) == 0 ? 0.0
                : Math.round(accepted * 1000.0 / (accepted + rejected)) / 10.0;

        return new DashboardResponse(
                admin, total, photos, footage, ready, missingKeywords, totalSub,
                submitted, inReview, accepted, rejected, acceptanceRate,
                mediaByStatus(owner),
                toCounts(subByStatus),
                mediaByMonth(owner),
                mediaByDevice(owner),
                acceptedRejectedBySite(owner));
    }

    // ----------------------------------------------------------------------

    private String ownerClause(UUID owner, String alias) {
        return owner == null ? "" : " and " + alias + ".ownerId = :owner";
    }

    private long countMedia(UUID owner, MediaType type) {
        String jpql = "select count(m) from MediaAsset m where m.deletedAt is null"
                + ownerClause(owner, "m") + (type != null ? " and m.mediaType = :type" : "");
        TypedQuery<Long> q = em.createQuery(jpql, Long.class);
        if (owner != null) q.setParameter("owner", owner);
        if (type != null) q.setParameter("type", type);
        return q.getSingleResult();
    }

    private long countMediaByStatus(UUID owner, WorkflowStatus status) {
        String jpql = "select count(m) from MediaAsset m where m.deletedAt is null"
                + ownerClause(owner, "m") + " and m.workflowStatus = :status";
        TypedQuery<Long> q = em.createQuery(jpql, Long.class).setParameter("status", status);
        if (owner != null) q.setParameter("owner", owner);
        return q.getSingleResult();
    }

    private long countMediaMissingKeywords(UUID owner) {
        String jpql = "select count(m) from MediaAsset m where m.deletedAt is null"
                + ownerClause(owner, "m") + " and m.keywords is empty";
        TypedQuery<Long> q = em.createQuery(jpql, Long.class);
        if (owner != null) q.setParameter("owner", owner);
        return q.getSingleResult();
    }

    private List<Count> mediaByStatus(UUID owner) {
        String jpql = "select m.workflowStatus, count(m) from MediaAsset m where m.deletedAt is null"
                + ownerClause(owner, "m") + " group by m.workflowStatus";
        TypedQuery<Object[]> q = em.createQuery(jpql, Object[].class);
        if (owner != null) q.setParameter("owner", owner);
        List<Count> out = new ArrayList<>();
        for (Object[] row : q.getResultList()) {
            out.add(new Count(((WorkflowStatus) row[0]).name(), (Long) row[1]));
        }
        return out;
    }

    private Map<SubmissionStatus, Long> submissionsByStatus(UUID owner) {
        String jpql = "select s.status, count(s) from SubmissionRecord s "
                + "where s.media.deletedAt is null" + ownerClause(owner, "s.media")
                + " group by s.status";
        TypedQuery<Object[]> q = em.createQuery(jpql, Object[].class);
        if (owner != null) q.setParameter("owner", owner);
        Map<SubmissionStatus, Long> map = new EnumMap<>(SubmissionStatus.class);
        for (Object[] row : q.getResultList()) {
            map.put((SubmissionStatus) row[0], (Long) row[1]);
        }
        return map;
    }

    private List<Count> toCounts(Map<SubmissionStatus, Long> map) {
        List<Count> out = new ArrayList<>();
        map.forEach((status, count) -> out.add(new Count(status.name(), count)));
        return out;
    }

    private List<Count> mediaByDevice(UUID owner) {
        String jpql = "select d.brand, d.model, count(m) from MediaAsset m join m.captureDevice d "
                + "where m.deletedAt is null" + ownerClause(owner, "m")
                + " group by d.id, d.brand, d.model order by count(m) desc";
        TypedQuery<Object[]> q = em.createQuery(jpql, Object[].class).setMaxResults(6);
        if (owner != null) q.setParameter("owner", owner);
        List<Count> out = new ArrayList<>();
        for (Object[] row : q.getResultList()) {
            out.add(new Count(row[0] + " " + row[1], (Long) row[2]));
        }
        return out;
    }

    /** Media created per calendar month (YYYY-MM). Native query for date formatting. */
    private List<Count> mediaByMonth(UUID owner) {
        String sql = "select to_char(created_at, 'YYYY-MM') ym, count(*) "
                + "from media_asset where deleted_at is null"
                + (owner == null ? "" : " and owner_id = :owner")
                + " group by ym order by ym";
        Query q = em.createNativeQuery(sql);
        if (owner != null) q.setParameter("owner", owner);
        List<Count> out = new ArrayList<>();
        @SuppressWarnings("unchecked")
        List<Object[]> rows = q.getResultList();
        for (Object[] row : rows) {
            out.add(new Count((String) row[0], ((Number) row[1]).longValue()));
        }
        return out;
    }

    private List<SiteReview> acceptedRejectedBySite(UUID owner) {
        String jpql = "select st.name, s.status, count(s) from SubmissionRecord s join s.stockSite st "
                + "where s.media.deletedAt is null" + ownerClause(owner, "s.media")
                + " and s.status in :statuses group by st.name, s.status order by st.name";
        TypedQuery<Object[]> q = em.createQuery(jpql, Object[].class)
                .setParameter("statuses", List.of(SubmissionStatus.ACCEPTED, SubmissionStatus.REJECTED));
        if (owner != null) q.setParameter("owner", owner);

        Map<String, long[]> bySite = new java.util.LinkedHashMap<>(); // [accepted, rejected]
        for (Object[] row : q.getResultList()) {
            String site = (String) row[0];
            SubmissionStatus status = (SubmissionStatus) row[1];
            long count = (Long) row[2];
            long[] pair = bySite.computeIfAbsent(site, k -> new long[2]);
            if (status == SubmissionStatus.ACCEPTED) pair[0] = count; else pair[1] = count;
        }
        List<SiteReview> out = new ArrayList<>();
        bySite.forEach((site, pair) -> out.add(new SiteReview(site, pair[0], pair[1])));
        return out;
    }
}
