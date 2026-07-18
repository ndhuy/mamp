package com.microstock.masterdata.service;

import com.microstock.common.error.ApiException;
import com.microstock.masterdata.domain.SiteCategory;
import com.microstock.masterdata.domain.StockSite;
import com.microstock.masterdata.repository.SiteCategoryRepository;
import com.microstock.masterdata.repository.StockSiteRepository;
import com.microstock.masterdata.web.dto.SiteCategoryRequest;
import com.microstock.masterdata.web.dto.SiteCategoryResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SiteCategoryService {

    private final SiteCategoryRepository repository;
    private final StockSiteRepository stockSiteRepository;

    public SiteCategoryService(SiteCategoryRepository repository, StockSiteRepository stockSiteRepository) {
        this.repository = repository;
        this.stockSiteRepository = stockSiteRepository;
    }

    @Transactional(readOnly = true)
    public List<SiteCategoryResponse> listForSite(UUID siteId, boolean includeInactive) {
        List<SiteCategory> categories = includeInactive
                ? repository.findByStockSiteIdOrderByNameAsc(siteId)
                : repository.findByStockSiteIdAndActiveTrueOrderByNameAsc(siteId);
        return categories.stream().map(SiteCategoryResponse::from).toList();
    }

    @Transactional
    public SiteCategoryResponse create(UUID siteId, SiteCategoryRequest req) {
        StockSite site = stockSiteRepository.findById(siteId)
                .orElseThrow(() -> ApiException.notFound("Stock site not found"));
        if (repository.existsByStockSiteIdAndNameIgnoreCase(siteId, req.name().trim())) {
            throw ApiException.conflict("A category with this name already exists for the site");
        }
        SiteCategory category = new SiteCategory(site, req.name().trim());
        category.setParent(resolveParent(siteId, req.parentId(), null));
        return SiteCategoryResponse.from(repository.save(category));
    }

    @Transactional
    public SiteCategoryResponse update(UUID id, SiteCategoryRequest req) {
        SiteCategory category = load(id);
        UUID siteId = category.getStockSite().getId();
        if (repository.existsByStockSiteIdAndNameIgnoreCaseAndIdNot(siteId, req.name().trim(), id)) {
            throw ApiException.conflict("A category with this name already exists for the site");
        }
        category.setName(req.name().trim());
        category.setParent(resolveParent(siteId, req.parentId(), id));
        return SiteCategoryResponse.from(category);
    }

    @Transactional
    public SiteCategoryResponse setActive(UUID id, boolean active) {
        SiteCategory category = load(id);
        category.setActive(active);
        return SiteCategoryResponse.from(category);
    }

    /** Parent must belong to the same site and cannot be the category itself. */
    private SiteCategory resolveParent(UUID siteId, UUID parentId, UUID selfId) {
        if (parentId == null) {
            return null;
        }
        if (parentId.equals(selfId)) {
            throw ApiException.badRequest("A category cannot be its own parent");
        }
        SiteCategory parent = repository.findById(parentId)
                .orElseThrow(() -> ApiException.badRequest("Parent category not found"));
        if (!parent.getStockSite().getId().equals(siteId)) {
            throw ApiException.badRequest("Parent category must belong to the same stock site");
        }
        return parent;
    }

    private SiteCategory load(UUID id) {
        return repository.findById(id).orElseThrow(() -> ApiException.notFound("Site category not found"));
    }
}
