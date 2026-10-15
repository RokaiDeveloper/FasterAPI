package com.rokaidev.fasterapi.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class SpecificationBuilder<T> {
    
    private final Class<T> entityClass;
    private final Set<String> allowedFields;
    private final boolean strict;
    private final ObjectMapper objectMapper;
    
    public SpecificationBuilder(Class<T> entityClass, Set<String> allowedFields, boolean strict) {
        this.entityClass = entityClass;
        this.allowedFields = allowedFields;
        this.strict = strict;
        this.objectMapper = new ObjectMapper();
    }
    
    public Specification<T> build(Map<String, String> filters) {
        List<FilterDefinition> definitions = prepareFilters(filters);
        
        return (Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            for (FilterDefinition definition : definitions) {
                switch (definition.operation()) {
                    case "eq" -> predicates.add(cb.equal(
                            root.get(definition.fieldName()), definition.value()));
                    case "like" -> predicates.add(cb.like(
                            root.get(definition.fieldName()), "%" + definition.rawValue() + "%"));
                    case "gt" -> predicates.add(cb.gt(
                            root.get(definition.fieldName()), (Number) definition.value()));
                    case "lt" -> predicates.add(cb.lt(
                            root.get(definition.fieldName()), (Number) definition.value()));
                    case "gte" -> predicates.add(cb.ge(
                            root.get(definition.fieldName()), (Number) definition.value()));
                    case "lte" -> predicates.add(cb.le(
                            root.get(definition.fieldName()), (Number) definition.value()));
                }
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private List<FilterDefinition> prepareFilters(Map<String, String> filters) {
        List<FilterDefinition> definitions = new ArrayList<>();
        if (filters == null) {
            return definitions;
        }

        filters.forEach((field, value) -> {
            String[] parts = field.split("__", 2);
            String fieldName = parts[0];
            String operation = parts.length > 1
                    ? parts[1].toLowerCase(Locale.ROOT)
                    : "eq";
            
            if (!Set.of("eq", "like", "gt", "lt", "gte", "lte").contains(operation)) {
                if (strict) {
                    throw invalidOperator(operation);
                }
                return;
            }
            
            if (!allowedFields.isEmpty() && !allowedFields.contains(fieldName)) {
                if (strict) {
                    throw new FilterValidationException(
                            "Campo '" + fieldName + "' não é permitido para filtro. Campos permitidos: " + allowedFields
                    );
                }
                return;
            }
            
            try {
                Field entityField = findField(entityClass, fieldName);
                Class<?> fieldType = entityField.getType();
                if ("like".equals(operation) && fieldType != String.class) {
                    if (strict) {
                        throw new FilterValidationException(
                                "Operador 'like' só pode ser usado em campos do tipo String");
                    }
                    return;
                }
                if (isComparison(operation) && !isNumericType(fieldType)) {
                    if (strict) {
                        throw new FilterValidationException(
                                "Operador '" + operation + "' só pode ser usado em campos numéricos");
                    }
                    return;
                }

                Object convertedValue;
                try {
                    convertedValue = "like".equals(operation)
                            ? value
                            : convertValue(value, fieldType);
                } catch (IllegalArgumentException ex) {
                    if (strict) {
                        throw new FilterValidationException(
                                "Valor '" + value + "' inválido para o campo '" + fieldName + "'", ex);
                    }
                    return;
                }
                definitions.add(new FilterDefinition(fieldName, operation, value, convertedValue));
            } catch (NoSuchFieldException e) {
                if (strict) {
                    throw new FilterValidationException(
                            "Campo '" + fieldName + "' não encontrado na entidade " + entityClass.getSimpleName()
                    );
                }
            }
        });
        return definitions;
    }

    private FilterValidationException invalidOperator(String operation) {
        return new FilterValidationException(
                "Operador '" + operation + "' não suportado. Operadores válidos: eq, like, gt, lt, gte, lte");
    }

    private boolean isComparison(String operation) {
        return Set.of("gt", "lt", "gte", "lte").contains(operation);
    }
    
    private Field findField(Class<?> type, String fieldName) throws NoSuchFieldException {
        Class<?> current = type;
        while (current != null) {
            try {
                return current.getDeclaredField(fieldName);
            } catch (NoSuchFieldException ignored) {
                current = current.getSuperclass();
            }
        }
        throw new NoSuchFieldException(fieldName);
    }
    
    private boolean isNumericType(Class<?> type) {
        return Number.class.isAssignableFrom(type)
                || type == byte.class
                || type == short.class
                || type == int.class
                || type == long.class
                || type == float.class
                || type == double.class;
    }
    
    private Object convertValue(String value, Class<?> targetType) {
        return objectMapper.convertValue(value, targetType);
    }

    private record FilterDefinition(String fieldName, String operation, String rawValue, Object value) {
    }
}
