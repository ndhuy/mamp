package com.microstock.media.repository;

import com.microstock.media.domain.MediaAsset;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * All read paths are owner-aware. Admin-scoped variants omit the owner filter;
 * callers must have verified the admin role first (see MediaService).
 */
public interface MediaAssetRepository
        extends JpaRepository<MediaAsset, UUID>, JpaSpecificationExecutor<MediaAsset> {

    @EntityGraph(attributePaths = {"captureDevice", "lens", "concepts", "keywords"})
    Optional<MediaAsset> findWithDetailsById(UUID id);

    // --- Owner-scoped (User) ---
    Page<MediaAsset> findByOwnerIdAndDeletedAtIsNull(UUID ownerId, Pageable pageable);

    Page<MediaAsset> findByOwnerIdAndDeletedAtIsNotNull(UUID ownerId, Pageable pageable);

    // --- Unscoped (Admin) ---
    Page<MediaAsset> findByDeletedAtIsNull(Pageable pageable);
}
