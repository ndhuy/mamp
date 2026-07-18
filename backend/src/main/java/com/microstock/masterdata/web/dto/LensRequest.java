package com.microstock.masterdata.web.dto;

import com.microstock.common.domain.LensType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LensRequest(
        @NotBlank @Size(max = 120) String brand,
        @NotBlank @Size(max = 120) String model,
        String mount,
        LensType lensType,
        Integer minFocalLength,
        Integer maxFocalLength,
        String maxAperture,
        String notes) {
}
