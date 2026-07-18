package com.microstock.common.security;

import com.microstock.common.error.ApiException;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/** Convenience accessors for the current authenticated principal. */
public final class SecurityUtils {

    private SecurityUtils() {}

    public static AppUserPrincipal currentPrincipal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof AppUserPrincipal principal)) {
            throw ApiException.unauthorized("No authenticated user");
        }
        return principal;
    }

    public static UUID currentUserId() {
        return currentPrincipal().id();
    }

    public static boolean isAdmin() {
        return currentPrincipal().isAdmin();
    }
}
