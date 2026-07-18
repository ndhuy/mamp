package com.microstock.common.security;

import com.microstock.common.domain.OwnedEntity;
import com.microstock.common.error.ApiException;
import org.springframework.stereotype.Component;

/**
 * Central enforcement point for ownership isolation (BR-002, BR-003, VAL-015).
 *
 * <p>A User may only touch their own private data; an Administrator may touch
 * anyone's. Denials are surfaced as 404 (not 403) so the API never reveals that
 * a record exists to a caller who is not allowed to see it.
 */
@Component
public class OwnershipGuard {

    /** Throws (as not-found) unless the current principal may access {@code entity}. */
    public void assertCanAccess(OwnedEntity entity) {
        if (entity == null) {
            throw ApiException.notFound("Resource not found");
        }
        AppUserPrincipal principal = SecurityUtils.currentPrincipal();
        if (principal.isAdmin()) {
            return;
        }
        if (!entity.getOwnerId().equals(principal.id())) {
            throw ApiException.forbiddenAsNotFound("Resource not found");
        }
    }

    public boolean isAdmin() {
        return SecurityUtils.isAdmin();
    }
}
