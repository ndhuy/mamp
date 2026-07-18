package com.microstock.concept.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ConceptRequest(
        @NotBlank @Size(max = 120) String name,
        String description) {
}
