package com.microstock.concept.web;

import com.microstock.concept.service.ConceptService;
import com.microstock.concept.web.dto.ConceptRequest;
import com.microstock.concept.web.dto.ConceptResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/concepts")
public class ConceptController {

    private final ConceptService service;

    public ConceptController(ConceptService service) {
        this.service = service;
    }

    @GetMapping
    public List<ConceptResponse> list() {
        return service.listOwn();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ConceptResponse create(@Valid @RequestBody ConceptRequest request) {
        return service.create(request);
    }
}
