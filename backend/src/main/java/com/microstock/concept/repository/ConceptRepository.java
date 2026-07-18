package com.microstock.concept.repository;

import com.microstock.concept.domain.Concept;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConceptRepository extends JpaRepository<Concept, UUID> {

    List<Concept> findByOwnerIdOrderByNameAsc(UUID ownerId);

    /** Owner-scoped lookup used when attaching concepts to media (BR-012). */
    List<Concept> findByOwnerIdAndIdIn(UUID ownerId, List<UUID> ids);

    boolean existsByOwnerIdAndNameIgnoreCase(UUID ownerId, String name);
}
