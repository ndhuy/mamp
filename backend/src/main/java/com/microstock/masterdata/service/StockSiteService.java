package com.microstock.masterdata.service;

import com.microstock.common.error.ApiException;
import com.microstock.common.util.Normalizer;
import com.microstock.masterdata.domain.StockSite;
import com.microstock.masterdata.repository.StockSiteRepository;
import com.microstock.masterdata.web.dto.StockSiteRequest;
import com.microstock.masterdata.web.dto.StockSiteResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StockSiteService {

    private final StockSiteRepository repository;

    public StockSiteService(StockSiteRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<StockSiteResponse> list(boolean includeInactive) {
        List<StockSite> sites = includeInactive
                ? repository.findAllByOrderByDisplayOrderAscNameAsc()
                : repository.findByActiveTrueOrderByDisplayOrderAscNameAsc();
        return sites.stream().map(StockSiteResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public StockSiteResponse get(UUID id) {
        return StockSiteResponse.from(load(id));
    }

    @Transactional
    public StockSiteResponse create(StockSiteRequest req) {
        String norm = Normalizer.normalize(req.name());
        if (repository.existsByNormalizedName(norm)) {
            throw ApiException.conflict("A stock site with this name already exists"); // VAL-011
        }
        StockSite site = new StockSite(req.name().trim(), norm);
        apply(site, req);
        return StockSiteResponse.from(repository.save(site));
    }

    @Transactional
    public StockSiteResponse update(UUID id, StockSiteRequest req) {
        StockSite site = load(id);
        String norm = Normalizer.normalize(req.name());
        if (repository.existsByNormalizedNameAndIdNot(norm, id)) {
            throw ApiException.conflict("A stock site with this name already exists");
        }
        site.setName(req.name().trim());
        site.setNormalizedName(norm);
        apply(site, req);
        return StockSiteResponse.from(site);
    }

    @Transactional
    public StockSiteResponse setActive(UUID id, boolean active) {
        StockSite site = load(id);
        site.setActive(active);
        return StockSiteResponse.from(site);
    }

    private void apply(StockSite site, StockSiteRequest req) {
        site.setWebsite(req.website());
        site.setDashboardUrl(req.dashboardUrl());
        site.setNotes(req.notes());
        site.setDisplayOrder(req.displayOrder() == null ? 0 : req.displayOrder());
        site.setCategoriesRequired(req.categoriesRequired() == null ? 0 : req.categoriesRequired());
    }

    private StockSite load(UUID id) {
        return repository.findById(id).orElseThrow(() -> ApiException.notFound("Stock site not found"));
    }
}
