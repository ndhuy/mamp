package com.microstock.concept.service;

import com.microstock.common.error.ApiException;
import com.microstock.common.security.SecurityUtils;
import com.microstock.concept.domain.Concept;
import com.microstock.concept.repository.ConceptRepository;
import com.microstock.concept.web.dto.ConceptRequest;
import com.microstock.concept.web.dto.ConceptResponse;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Concepts are private to each User (BR-012). All operations are owner-scoped. */
@Service
public class ConceptService {

    private final ConceptRepository repository;

    public ConceptService(ConceptRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<ConceptResponse> listOwn() {
        return repository.findByOwnerIdOrderByNameAsc(SecurityUtils.currentUserId())
                .stream().map(ConceptResponse::from).toList();
    }

    @Transactional
    public ConceptResponse create(ConceptRequest req) {
        var ownerId = SecurityUtils.currentUserId();
        if (repository.existsByOwnerIdAndNameIgnoreCase(ownerId, req.name().trim())) {
            throw ApiException.conflict("You already have a concept with this name");
        }
        Concept concept = new Concept(ownerId, req.name().trim(), req.description());
        return ConceptResponse.from(repository.save(concept));
    }
}
