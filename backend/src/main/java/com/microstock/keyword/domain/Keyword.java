package com.microstock.keyword.domain;

import com.microstock.common.domain.OwnedEntity;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

/** Private, normalized keyword. Unique per owner by normalized value (PRD 6.5). */
@Entity
@Table(name = "keyword")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Keyword implements OwnedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "owner_id", nullable = false, updatable = false)
    private UUID ownerId;

    /** Display form as entered. */
    @Column(nullable = false)
    private String value;

    /** Trimmed, whitespace-collapsed, lowercased form used for search/uniqueness. */
    @Column(name = "normalized_value", nullable = false)
    private String normalizedValue;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public Keyword(UUID ownerId, String value, String normalizedValue) {
        this.ownerId = ownerId;
        this.value = value;
        this.normalizedValue = normalizedValue;
    }

    /** PRD 6.5 normalization: trim, collapse repeated spaces, lowercase. */
    public static String normalize(String raw) {
        if (raw == null) {
            return null;
        }
        return raw.trim().replaceAll("\\s+", " ").toLowerCase();
    }
}
