package com.microstock.concept.web.dto;

import com.microstock.concept.domain.Concept;
import java.util.UUID;

public record ConceptResponse(UUID id, String name, String description, boolean active) {

    public static ConceptResponse from(Concept c) {
        return new ConceptResponse(c.getId(), c.getName(), c.getDescription(), c.isActive());
    }
}
