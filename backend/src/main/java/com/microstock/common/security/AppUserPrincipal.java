package com.microstock.common.security;

import com.microstock.common.domain.Role;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Authenticated principal carried in the SecurityContext. Built from the JWT
 * claims on each request (stateless) — no DB round-trip per call.
 */
public record AppUserPrincipal(UUID id, String username, Role role) implements UserDetails {

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }

    @Override
    public String getPassword() {
        return null; // not used; authentication is via JWT
    }

    @Override
    public String getUsername() {
        return username;
    }
}
