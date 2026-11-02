package com.rokaidev.fasterapi.web;

import java.util.Map;

import org.springframework.data.jpa.domain.Specification;

public class FasterFilters<T> {
    private final Map<String, String> filters;
    private final Specification<T> specification;

    public FasterFilters(Map<String, String> filters, Specification<T> specification) {
        this.filters = filters;
        this.specification = specification;
    }

    public Map<String, String> getFilters() {
        return filters;
    }

    public Specification<T> getSpecification() {
        return specification;
    }
}
