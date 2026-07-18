package com.microstock.concept.domain;

import com.microstock.common.domain.OwnedEntity;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/** Private thematic classification, unique per owner by name (PRD 6.5). */
@Entity
@Table(name = "concept")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Concept implements OwnedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "owner_id", nullable = false, updatable = false)
    private UUID ownerId;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "text")
    private String description;

    @Column(nullable = false)
    private boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public Concept(UUID ownerId, String name, String description) {
        this.ownerId = ownerId;
        this.name = name;
        this.description = description;
    }
}
