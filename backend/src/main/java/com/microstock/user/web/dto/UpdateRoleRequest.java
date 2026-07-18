package com.microstock.user.web.dto;

import com.microstock.common.domain.Role;
import jakarta.validation.constraints.NotNull;

public record UpdateRoleRequest(@NotNull Role role) {
}
