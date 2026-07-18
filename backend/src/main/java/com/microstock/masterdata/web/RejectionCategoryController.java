package com.microstock.masterdata.web;

import com.microstock.masterdata.service.RejectionCategoryService;
import com.microstock.masterdata.web.dto.ActiveRequest;
import com.microstock.masterdata.web.dto.RejectionCategoryRequest;
import com.microstock.masterdata.web.dto.RejectionCategoryResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rejection-categories")
public class RejectionCategoryController {

    private final RejectionCategoryService service;

    public RejectionCategoryController(RejectionCategoryService service) {
        this.service = service;
    }

    @GetMapping
    public List<RejectionCategoryResponse> list(@RequestParam(defaultValue = "false") boolean includeInactive) {
        return service.list(includeInactive);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public RejectionCategoryResponse create(@Valid @RequestBody RejectionCategoryRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public RejectionCategoryResponse update(@PathVariable UUID id, @Valid @RequestBody RejectionCategoryRequest request) {
        return service.update(id, request);
    }

    @PatchMapping("/{id}/active")
    @PreAuthorize("hasRole('ADMIN')")
    public RejectionCategoryResponse setActive(@PathVariable UUID id, @Valid @RequestBody ActiveRequest request) {
        return service.setActive(id, request.active());
    }
}
