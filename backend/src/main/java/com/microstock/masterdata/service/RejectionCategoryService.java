package com.microstock.masterdata.service;

import com.microstock.common.error.ApiException;
import com.microstock.masterdata.domain.RejectionCategory;
import com.microstock.masterdata.repository.RejectionCategoryRepository;
import com.microstock.masterdata.web.dto.RejectionCategoryRequest;
import com.microstock.masterdata.web.dto.RejectionCategoryResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RejectionCategoryService {

    private final RejectionCategoryRepository repository;

    public RejectionCategoryService(RejectionCategoryRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<RejectionCategoryResponse> list(boolean includeInactive) {
        List<RejectionCategory> items = includeInactive
                ? repository.findAllByOrderByNameAsc()
                : repository.findByActiveTrueOrderByNameAsc();
        return items.stream().map(RejectionCategoryResponse::from).toList();
    }

    @Transactional
    public RejectionCategoryResponse create(RejectionCategoryRequest req) {
        if (repository.existsByNameIgnoreCase(req.name().trim())) {
            throw ApiException.conflict("A rejection category with this name already exists");
        }
        return RejectionCategoryResponse.from(repository.save(new RejectionCategory(req.name().trim())));
    }

    @Transactional
    public RejectionCategoryResponse update(UUID id, RejectionCategoryRequest req) {
        RejectionCategory item = load(id);
        if (repository.existsByNameIgnoreCaseAndIdNot(req.name().trim(), id)) {
            throw ApiException.conflict("A rejection category with this name already exists");
        }
        item.setName(req.name().trim());
        return RejectionCategoryResponse.from(item);
    }

    @Transactional
    public RejectionCategoryResponse setActive(UUID id, boolean active) {
        RejectionCategory item = load(id);
        item.setActive(active);
        return RejectionCategoryResponse.from(item);
    }

    private RejectionCategory load(UUID id) {
        return repository.findById(id).orElseThrow(() -> ApiException.notFound("Rejection category not found"));
    }
}
