package com.rokaidev.fasterapi.web;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;

import java.util.List;
import java.util.Map;

/**
 * High-level query facade injected by {@link com.rokaidev.fasterapi.annotation.FasterFilter}.
 * It keeps Spring Data and JPA implementation details out of custom controllers.
 */
public class FasterFilterQuery<T> {

    private final Map<String, String> filters;
    private final Specification<T> specification;
    private final SimpleJpaRepository<T, Long> repository;

    public FasterFilterQuery(Map<String, String> filters,
                             Specification<T> specification,
                             SimpleJpaRepository<T, Long> repository) {
        this.filters = filters;
        this.specification = specification;
        this.repository = repository;
    }

    public List<T> findAll() {
        return repository.findAll(specification);
    }

    public Page<T> findAll(Pageable pageable) {
        return repository.findAll(specification, pageable);
    }

    public Map<String, String> getFilters() {
        return filters;
    }
}
