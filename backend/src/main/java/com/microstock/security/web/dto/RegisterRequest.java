package com.microstock.security.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Registration payload (AUTH-001, VAL-001..003). */
public record RegisterRequest(
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank @Size(min = 3, max = 50)
        @Pattern(regexp = "^[A-Za-z0-9_.-]+$",
                message = "Username may contain letters, numbers, and . _ - only")
        String username,
        @NotBlank @Size(min = 8, max = 100) String password,
        @NotBlank String confirmPassword) {
}
