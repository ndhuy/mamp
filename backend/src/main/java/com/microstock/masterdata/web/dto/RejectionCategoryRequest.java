package com.microstock.masterdata.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RejectionCategoryRequest(@NotBlank @Size(max = 120) String name) {
}
