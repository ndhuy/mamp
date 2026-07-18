package com.microstock.security.web.dto;

import com.microstock.common.domain.Role;
import com.microstock.common.domain.UserStatus;
import com.microstock.user.domain.User;
import java.util.UUID;

public record UserResponse(UUID id, String email, String username, Role role, UserStatus status) {

    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(), user.getEmail(), user.getUsername(), user.getRole(), user.getStatus());
    }
}
