package com.microstock.masterdata.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

/** Global standardized rejection reason used for analysis (PRD 6.12, BR-010). */
@Entity
@Table(name = "rejection_category")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RejectionCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public RejectionCategory(String name) {
        this.name = name;
    }
}
