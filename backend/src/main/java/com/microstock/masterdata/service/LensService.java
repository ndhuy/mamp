package com.microstock.masterdata.service;

import com.microstock.common.error.ApiException;
import com.microstock.common.util.Normalizer;
import com.microstock.masterdata.domain.Lens;
import com.microstock.masterdata.repository.LensRepository;
import com.microstock.masterdata.web.dto.LensRequest;
import com.microstock.masterdata.web.dto.LensResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LensService {

    private final LensRepository repository;

    public LensService(LensRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<LensResponse> list(boolean includeInactive) {
        List<Lens> lenses = includeInactive
                ? repository.findAllByOrderByBrandAscModelAsc()
                : repository.findByActiveTrueOrderByBrandAscModelAsc();
        return lenses.stream().map(LensResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public LensResponse get(UUID id) {
        return LensResponse.from(load(id));
    }

    @Transactional
    public LensResponse create(LensRequest req) {
        String key = Normalizer.key(req.brand(), req.model());
        if (repository.existsByNormalizedKey(key)) {
            throw ApiException.conflict("A lens with this brand and model already exists"); // VAL-010
        }
        Lens lens = new Lens(req.brand().trim(), req.model().trim(), key);
        apply(lens, req);
        return LensResponse.from(repository.save(lens));
    }

    @Transactional
    public LensResponse update(UUID id, LensRequest req) {
        Lens lens = load(id);
        String key = Normalizer.key(req.brand(), req.model());
        if (repository.existsByNormalizedKeyAndIdNot(key, id)) {
            throw ApiException.conflict("A lens with this brand and model already exists");
        }
        lens.setBrand(req.brand().trim());
        lens.setModel(req.model().trim());
        lens.setNormalizedKey(key);
        apply(lens, req);
        return LensResponse.from(lens);
    }

    @Transactional
    public LensResponse setActive(UUID id, boolean active) {
        Lens lens = load(id);
        lens.setActive(active);
        return LensResponse.from(lens);
    }

    private void apply(Lens lens, LensRequest req) {
        lens.setMount(req.mount());
        lens.setLensType(req.lensType());
        lens.setMinFocalLength(req.minFocalLength());
        lens.setMaxFocalLength(req.maxFocalLength());
        lens.setMaxAperture(req.maxAperture());
        lens.setNotes(req.notes());
    }

    private Lens load(UUID id) {
        return repository.findById(id).orElseThrow(() -> ApiException.notFound("Lens not found"));
    }
}
