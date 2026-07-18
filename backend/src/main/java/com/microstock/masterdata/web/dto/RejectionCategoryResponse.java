package com.microstock.masterdata.web.dto;

import com.microstock.masterdata.domain.RejectionCategory;
import java.util.UUID;

public record RejectionCategoryResponse(UUID id, String name, boolean active) {

    public static RejectionCategoryResponse from(RejectionCategory r) {
        return new RejectionCategoryResponse(r.getId(), r.getName(), r.isActive());
    }
}
