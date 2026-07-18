package com.microstock.masterdata.web;

import com.microstock.masterdata.service.SiteCategoryService;
import com.microstock.masterdata.service.StockSiteService;
import com.microstock.masterdata.web.dto.*;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stock-sites")
public class StockSiteController {

    private final StockSiteService service;
    private final SiteCategoryService categoryService;

    public StockSiteController(StockSiteService service, SiteCategoryService categoryService) {
        this.service = service;
        this.categoryService = categoryService;
    }

    // --- Stock sites ---

    @GetMapping
    public List<StockSiteResponse> list(@RequestParam(defaultValue = "false") boolean includeInactive) {
        return service.list(includeInactive);
    }

    @GetMapping("/{id}")
    public StockSiteResponse get(@PathVariable UUID id) {
        return service.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public StockSiteResponse create(@Valid @RequestBody StockSiteRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public StockSiteResponse update(@PathVariable UUID id, @Valid @RequestBody StockSiteRequest request) {
        return service.update(id, request);
    }

    @PatchMapping("/{id}/active")
    @PreAuthorize("hasRole('ADMIN')")
    public StockSiteResponse setActive(@PathVariable UUID id, @Valid @RequestBody ActiveRequest request) {
        return service.setActive(id, request.active());
    }

    // --- Site categories (nested under a site) ---

    @GetMapping("/{siteId}/categories")
    public List<SiteCategoryResponse> listCategories(
            @PathVariable UUID siteId, @RequestParam(defaultValue = "false") boolean includeInactive) {
        return categoryService.listForSite(siteId, includeInactive);
    }

    @PostMapping("/{siteId}/categories")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public SiteCategoryResponse createCategory(
            @PathVariable UUID siteId, @Valid @RequestBody SiteCategoryRequest request) {
        return categoryService.create(siteId, request);
    }

    @PutMapping("/categories/{categoryId}")
    @PreAuthorize("hasRole('ADMIN')")
    public SiteCategoryResponse updateCategory(
            @PathVariable UUID categoryId, @Valid @RequestBody SiteCategoryRequest request) {
        return categoryService.update(categoryId, request);
    }

    @PatchMapping("/categories/{categoryId}/active")
    @PreAuthorize("hasRole('ADMIN')")
    public SiteCategoryResponse setCategoryActive(
            @PathVariable UUID categoryId, @Valid @RequestBody ActiveRequest request) {
        return categoryService.setActive(categoryId, request.active());
    }
}
