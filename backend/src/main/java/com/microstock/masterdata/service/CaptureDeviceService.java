package com.microstock.masterdata.service;

import com.microstock.common.error.ApiException;
import com.microstock.common.util.Normalizer;
import com.microstock.masterdata.domain.CaptureDevice;
import com.microstock.masterdata.repository.CaptureDeviceRepository;
import com.microstock.masterdata.web.dto.CaptureDeviceRequest;
import com.microstock.masterdata.web.dto.CaptureDeviceResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CaptureDeviceService {

    private final CaptureDeviceRepository repository;

    public CaptureDeviceService(CaptureDeviceRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<CaptureDeviceResponse> list(boolean includeInactive) {
        List<CaptureDevice> devices = includeInactive
                ? repository.findAllByOrderByBrandAscModelAsc()
                : repository.findByActiveTrueOrderByBrandAscModelAsc();
        return devices.stream().map(CaptureDeviceResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public CaptureDeviceResponse get(UUID id) {
        return CaptureDeviceResponse.from(load(id));
    }

    @Transactional
    public CaptureDeviceResponse create(CaptureDeviceRequest req) {
        String key = Normalizer.key(req.brand(), req.model());
        if (repository.existsByNormalizedKey(key)) {
            throw ApiException.conflict("A capture device with this brand and model already exists"); // VAL-010
        }
        CaptureDevice device = new CaptureDevice(req.brand().trim(), req.model().trim(), key, req.deviceType());
        apply(device, req);
        return CaptureDeviceResponse.from(repository.save(device));
    }

    @Transactional
    public CaptureDeviceResponse update(UUID id, CaptureDeviceRequest req) {
        CaptureDevice device = load(id);
        String key = Normalizer.key(req.brand(), req.model());
        if (repository.existsByNormalizedKeyAndIdNot(key, id)) {
            throw ApiException.conflict("A capture device with this brand and model already exists");
        }
        device.setBrand(req.brand().trim());
        device.setModel(req.model().trim());
        device.setNormalizedKey(key);
        device.setDeviceType(req.deviceType());
        apply(device, req);
        return CaptureDeviceResponse.from(device);
    }

    /** Inactivate/reactivate rather than delete (PRD 6.6 — preserves historical media). */
    @Transactional
    public CaptureDeviceResponse setActive(UUID id, boolean active) {
        CaptureDevice device = load(id);
        device.setActive(active);
        return CaptureDeviceResponse.from(device);
    }

    private void apply(CaptureDevice device, CaptureDeviceRequest req) {
        device.setMount(req.mount());
        device.setSerialNumber(req.serialNumber());
        device.setNotes(req.notes());
    }

    private CaptureDevice load(UUID id) {
        return repository.findById(id).orElseThrow(() -> ApiException.notFound("Capture device not found"));
    }
}
