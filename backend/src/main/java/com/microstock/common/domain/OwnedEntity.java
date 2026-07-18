package com.microstock.common.domain;

import java.util.UUID;

/**
 * Marks private data that belongs to exactly one User (BR-001, BR-002).
 * The ownership guard uses this to enforce isolation uniformly.
 */
public interface OwnedEntity {
    UUID getOwnerId();
}
