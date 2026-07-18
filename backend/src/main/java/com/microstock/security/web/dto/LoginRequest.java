package com.microstock.security.web.dto;

import jakarta.validation.constraints.NotBlank;

/** Login by email OR username (AUTH-002). */
public record LoginRequest(
        @NotBlank String identifier,
        @NotBlank String password) {
}
