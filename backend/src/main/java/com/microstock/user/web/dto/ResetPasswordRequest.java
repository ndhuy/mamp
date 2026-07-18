package com.microstock.user.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Administrator-initiated password reset (AUTH-005). */
public record ResetPasswordRequest(@NotBlank @Size(min = 8, max = 100) String newPassword) {
}
