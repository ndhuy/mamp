package com.microstock.masterdata.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/** Global master data: a microstock marketplace/agency (PRD 6.9). */
@Entity
@Table(name = "stock_site")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StockSite {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(name = "normalized_name", nullable = false, unique = true)
    private String normalizedName;

    private String website;

    @Column(name = "dashboard_url")
    private String dashboardUrl;

    @Column(columnDefinition = "text")
    private String notes;

    @Column(name = "display_order", nullable = false)
    private int displayOrder = 0;

    /** How many site categories a submission to this site requires: 0, 1, or 2 (PRD 6.10). */
    @Column(name = "categories_required", nullable = false)
    private int categoriesRequired = 0;

    @Column(nullable = false)
    private boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public StockSite(String name, String normalizedName) {
        this.name = name;
        this.normalizedName = normalizedName;
    }
}
