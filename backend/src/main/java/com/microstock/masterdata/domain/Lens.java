package com.microstock.masterdata.domain;

import com.microstock.common.domain.LensType;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/** Global master data: an interchangeable lens (PRD 6.7). Optional on media. */
@Entity
@Table(name = "lens")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Lens {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false)
    private String brand;

    @Column(nullable = false)
    private String model;

    @Column(name = "normalized_key", nullable = false, unique = true)
    private String normalizedKey;

    private String mount;

    @Enumerated(EnumType.STRING)
    @Column(name = "lens_type", length = 20)
    private LensType lensType;

    @Column(name = "min_focal_length")
    private Integer minFocalLength;

    @Column(name = "max_focal_length")
    private Integer maxFocalLength;

    @Column(name = "max_aperture")
    private String maxAperture;

    @Column(columnDefinition = "text")
    private String notes;

    @Column(nullable = false)
    private boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public Lens(String brand, String model, String normalizedKey) {
        this.brand = brand;
        this.model = model;
        this.normalizedKey = normalizedKey;
    }
}
