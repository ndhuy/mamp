package com.microstock.masterdata.web;

import com.microstock.masterdata.service.CaptureDeviceService;
import com.microstock.masterdata.web.dto.ActiveRequest;
import com.microstock.masterdata.web.dto.CaptureDeviceRequest;
import com.microstock.masterdata.web.dto.CaptureDeviceResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/capture-devices")
public class CaptureDeviceController {

    private final CaptureDeviceService service;

    public CaptureDeviceController(CaptureDeviceService service) {
        this.service = service;
    }

    /** Authenticated users read active devices; admins may include inactive. */
    @GetMapping
    public List<CaptureDeviceResponse> list(@RequestParam(defaultValue = "false") boolean includeInactive) {
        return service.list(includeInactive);
    }

    @GetMapping("/{id}")
    public CaptureDeviceResponse get(@PathVariable UUID id) {
        return service.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public CaptureDeviceResponse create(@Valid @RequestBody CaptureDeviceRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public CaptureDeviceResponse update(@PathVariable UUID id, @Valid @RequestBody CaptureDeviceRequest request) {
        return service.update(id, request);
    }

    @PatchMapping("/{id}/active")
    @PreAuthorize("hasRole('ADMIN')")
    public CaptureDeviceResponse setActive(@PathVariable UUID id, @Valid @RequestBody ActiveRequest request) {
        return service.setActive(id, request.active());
    }
}
