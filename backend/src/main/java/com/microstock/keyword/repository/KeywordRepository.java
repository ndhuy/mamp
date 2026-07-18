package com.microstock.keyword.repository;

import com.microstock.keyword.domain.Keyword;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KeywordRepository extends JpaRepository<Keyword, UUID> {

    Optional<Keyword> findByOwnerIdAndNormalizedValue(UUID ownerId, String normalizedValue);

    List<Keyword> findByOwnerIdOrderByNormalizedValueAsc(UUID ownerId);

    /** Autocomplete suggestions (partial match) — never a substitute for exact filtering. */
    List<Keyword> findTop10ByOwnerIdAndNormalizedValueContainingOrderByNormalizedValueAsc(
            UUID ownerId, String fragment);
}
