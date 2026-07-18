package com.microstock.masterdata.web.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record StockSiteRequest(
        @NotBlank @Size(max = 120) String name,
        String website,
        String dashboardUrl,
        String notes,
        Integer displayOrder,
        @Min(0) @Max(2) Integer categoriesRequired) {
}
