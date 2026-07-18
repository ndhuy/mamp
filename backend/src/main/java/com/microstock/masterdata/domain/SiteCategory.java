package com.microstock.masterdata.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

/** Category vocabulary belonging to exactly one Stock Site (PRD 6.10). */
@Entity
@Table(name = "site_category")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SiteCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "stock_site_id", nullable = false, updatable = false)
    private StockSite stockSite;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private SiteCategory parent;

    @Column(nullable = false)
    private boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public SiteCategory(StockSite stockSite, String name) {
        this.stockSite = stockSite;
        this.name = name;
    }
}
