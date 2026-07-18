package com.microstock.masterdata.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record SiteCategoryRequest(
        @NotBlank @Size(max = 120) String name,
        UUID parentId) {
}
