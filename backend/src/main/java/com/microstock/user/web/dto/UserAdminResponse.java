package com.microstock.user.web.dto;

import com.microstock.common.domain.Role;
import com.microstock.common.domain.UserStatus;
import com.microstock.user.domain.User;
import java.time.Instant;
import java.util.UUID;

public record UserAdminResponse(
        UUID id,
        String email,
        String username,
        Role role,
        UserStatus status,
        Instant createdAt) {

    public static UserAdminResponse from(User u) {
        return new UserAdminResponse(
                u.getId(), u.getEmail(), u.getUsername(), u.getRole(), u.getStatus(), u.getCreatedAt());
    }
}
