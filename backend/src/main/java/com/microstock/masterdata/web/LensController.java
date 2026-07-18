package com.microstock.masterdata.web;

import com.microstock.masterdata.service.LensService;
import com.microstock.masterdata.web.dto.ActiveRequest;
import com.microstock.masterdata.web.dto.LensRequest;
import com.microstock.masterdata.web.dto.LensResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/lenses")
public class LensController {

    private final LensService service;

    public LensController(LensService service) {
        this.service = service;
    }

    @GetMapping
    public List<LensResponse> list(@RequestParam(defaultValue = "false") boolean includeInactive) {
        return service.list(includeInactive);
    }

    @GetMapping("/{id}")
    public LensResponse get(@PathVariable UUID id) {
        return service.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public LensResponse create(@Valid @RequestBody LensRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public LensResponse update(@PathVariable UUID id, @Valid @RequestBody LensRequest request) {
        return service.update(id, request);
    }

    @PatchMapping("/{id}/active")
    @PreAuthorize("hasRole('ADMIN')")
    public LensResponse setActive(@PathVariable UUID id, @Valid @RequestBody ActiveRequest request) {
        return service.setActive(id, request.active());
    }
}
