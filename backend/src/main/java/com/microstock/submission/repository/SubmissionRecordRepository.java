package com.microstock.submission.repository;

import com.microstock.submission.domain.SubmissionRecord;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubmissionRecordRepository extends JpaRepository<SubmissionRecord, UUID> {

    @EntityGraph(attributePaths = {"stockSite", "primaryCategory", "secondaryCategory", "rejectionCategory"})
    List<SubmissionRecord> findByMediaIdOrderByStockSite_DisplayOrderAscStockSite_NameAsc(UUID mediaId);

    @EntityGraph(attributePaths = {"media", "stockSite", "primaryCategory", "secondaryCategory", "rejectionCategory"})
    Optional<SubmissionRecord> findWithDetailsById(UUID id);

    boolean existsByMediaIdAndStockSiteId(UUID mediaId, UUID stockSiteId);

    /** Batch load for export — all submissions for the given media. */
    @EntityGraph(attributePaths = {"media", "stockSite", "primaryCategory", "secondaryCategory", "rejectionCategory"})
    List<SubmissionRecord> findByMediaIdIn(Collection<UUID> mediaIds);
}
