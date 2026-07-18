package com.microstock.masterdata.web.dto;

import com.microstock.common.domain.LensType;
import com.microstock.masterdata.domain.Lens;
import java.util.UUID;

public record LensResponse(
        UUID id,
        String brand,
        String model,
        String mount,
        LensType lensType,
        Integer minFocalLength,
        Integer maxFocalLength,
        String maxAperture,
        String notes,
        boolean active) {

    public static LensResponse from(Lens l) {
        return new LensResponse(
                l.getId(), l.getBrand(), l.getModel(), l.getMount(), l.getLensType(),
                l.getMinFocalLength(), l.getMaxFocalLength(), l.getMaxAperture(), l.getNotes(), l.isActive());
    }
}
