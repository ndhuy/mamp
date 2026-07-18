package com.microstock.dashboard.web.dto;

import java.util.List;

/** Aggregated metrics for the dashboard (PRD 8). Scope is owner or system-wide (admin). */
public record DashboardResponse(
        boolean systemWide,
        long totalMedia,
        long photos,
        long footage,
        long readyForUpload,
        long missingKeywords,
        long totalSubmissions,
        long submitted,
        long inReview,
        long accepted,
        long rejected,
        double acceptanceRate,
        List<Count> mediaByStatus,
        List<Count> submissionsByStatus,
        List<Count> mediaByMonth,
        List<Count> mediaByDevice,
        List<SiteReview> acceptedRejectedBySite) {

    /** Generic label/value pair for a chart series. */
    public record Count(String label, long value) {}

    /** Accepted vs rejected totals for one stock site. */
    public record SiteReview(String site, long accepted, long rejected) {}
}
