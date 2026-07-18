package com.microstock.masterdata.web.dto;

import com.microstock.masterdata.domain.StockSite;
import java.util.UUID;

public record StockSiteResponse(
        UUID id,
        String name,
        String website,
        String dashboardUrl,
        String notes,
        int displayOrder,
        int categoriesRequired,
        boolean active) {

    public static StockSiteResponse from(StockSite s) {
        return new StockSiteResponse(
                s.getId(), s.getName(), s.getWebsite(), s.getDashboardUrl(), s.getNotes(),
                s.getDisplayOrder(), s.getCategoriesRequired(), s.isActive());
    }
}
