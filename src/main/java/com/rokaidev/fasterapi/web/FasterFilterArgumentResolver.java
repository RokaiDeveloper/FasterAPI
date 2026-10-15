package com.rokaidev.fasterapi.web;

import com.rokaidev.fasterapi.annotation.FasterFilter;
import com.rokaidev.fasterapi.persistence.FilterValidationException;
import com.rokaidev.fasterapi.persistence.SpecificationBuilder;
import org.springframework.core.MethodParameter;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.JpaEntityInformationSupport;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import jakarta.persistence.EntityManager;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class FasterFilterArgumentResolver implements HandlerMethodArgumentResolver {

    private final EntityManager entityManager;

    public FasterFilterArgumentResolver(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        // Check if the parameter has @FasterFilter annotation
        return parameter.hasParameterAnnotation(FasterFilter.class)
                && (FasterFilters.class.isAssignableFrom(parameter.getParameterType())
                || FasterFilterQuery.class.isAssignableFrom(parameter.getParameterType()));
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        FasterFilter annotation = parameter.getParameterAnnotation(FasterFilter.class);
        if (annotation == null) {
            throw new IllegalStateException("@FasterFilter annotation not found on parameter");
        }

        Class<?> entityClass = annotation.entity();
        String[] allowedFields = annotation.fields();
        boolean strict = annotation.strict();

        Set<String> allowedFieldSet = Arrays.stream(allowedFields)
                .collect(Collectors.toSet());

        // Extract filter parameters from request
        Map<String, String> filters = new HashMap<>();
        webRequest.getParameterMap().forEach((key, values) -> {
            if (!isPaginationOrSortParam(key) && values.length > 0) {
                filters.put(key, values[0]);
            }
        });

        // Build and validate specification
        SpecificationBuilder<Object> builder = new SpecificationBuilder<>(
                (Class<Object>) entityClass,
                allowedFieldSet,
                strict
        );

        Specification<Object> specification;
        try {
            specification = builder.build(filters);
        } catch (FilterValidationException e) {
            throw e;
        } catch (Exception e) {
            throw new FilterValidationException("Erro ao construir filtros: " + e.getMessage(), e);
        }

        if (FasterFilters.class.isAssignableFrom(parameter.getParameterType())) {
            return new FasterFilters<>(filters, specification);
        }

        JpaEntityInformation<Object, Long> entityInformation =
                (JpaEntityInformation<Object, Long>) (JpaEntityInformation<?, ?>)
                        JpaEntityInformationSupport.getEntityInformation(entityClass, entityManager);
        SimpleJpaRepository<Object, Long> repository = new SimpleJpaRepository<>(
                entityInformation, entityManager);
        return new FasterFilterQuery<>(filters, specification, repository);
    }

    private boolean isPaginationOrSortParam(String paramName) {
        return paramName.equals("page")
                || paramName.equals("size")
                || paramName.equals("sort");
    }
}
