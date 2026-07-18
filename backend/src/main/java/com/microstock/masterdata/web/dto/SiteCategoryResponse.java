package com.microstock.masterdata.web.dto;

import com.microstock.masterdata.domain.SiteCategory;
import java.util.UUID;

public record SiteCategoryResponse(
        UUID id,
        UUID stockSiteId,
        String name,
        UUID parentId,
        String parentName,
        boolean active) {

    public static SiteCategoryResponse from(SiteCategory c) {
        return new SiteCategoryResponse(
                c.getId(),
                c.getStockSite().getId(),
                c.getName(),
                c.getParent() == null ? null : c.getParent().getId(),
                c.getParent() == null ? null : c.getParent().getName(),
                c.isActive());
    }
}
