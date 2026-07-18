package com.microstock.user.web.dto;

import com.microstock.common.domain.UserStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateStatusRequest(@NotNull UserStatus status) {
}
