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
        // Validate immediately before returning lazy specification
        validateFilters(filters);
        
        return (Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (filters != null) {
                filters.forEach((field, value) -> {
                    try {
                        String[] parts = field.split("__", 2);
                        String fieldName = parts[0];
                        String operation = parts.length > 1
                                ? parts[1].toLowerCase(Locale.ROOT)
                                : "eq";
                        
                        // Validate operation
                        if (!Set.of("eq", "like", "gt", "lt", "gte", "lte").contains(operation)) {
                            if (strict) {
                                throw new FilterValidationException(
                                    "Operador '" + operation + "' não suportado. Operadores válidos: eq, like, gt, lt, gte, lte"
                                );
                            }
                            return;
                        }
                        
                        // Validate field is allowed
                        if (!allowedFields.isEmpty() && !allowedFields.contains(fieldName)) {
                            if (strict) {
                                throw new FilterValidationException(
                                    "Campo '" + fieldName + "' não é permitido para filtro. Campos permitidos: " + allowedFields
                                );
                            }
                            return;
                        }
                        
                        Field entityField = findField(entityClass, fieldName);
                        entityField.setAccessible(true);
                        
                        switch (operation) {
                            case "eq":
                                predicates.add(cb.equal(root.get(fieldName), convertValue(value, entityField.getType())));
                                break;
                            case "like":
                                if (entityField.getType() == String.class) {
                                    predicates.add(cb.like(root.get(fieldName), "%" + value + "%"));
                                } else if (strict) {
                                    throw new FilterValidationException(
                                        "Operador 'like' só pode ser usado em campos do tipo String"
                                    );
                                }
                                break;
                            case "gt":
                                if (isNumericType(entityField.getType())) {
                                    predicates.add(cb.gt(root.get(fieldName), (Number) convertValue(value, entityField.getType())));
                                } else if (strict) {
                                    throw new FilterValidationException(
                                        "Operador 'gt' só pode ser usado em campos numéricos"
                                    );
                                }
                                break;
                            case "lt":
                                if (isNumericType(entityField.getType())) {
                                    predicates.add(cb.lt(root.get(fieldName), (Number) convertValue(value, entityField.getType())));
                                } else if (strict) {
                                    throw new FilterValidationException(
                                        "Operador 'lt' só pode ser usado em campos numéricos"
                                    );
                                }
                                break;
                            case "gte":
                                if (isNumericType(entityField.getType())) {
                                    predicates.add(cb.ge(root.get(fieldName), (Number) convertValue(value, entityField.getType())));
                                } else if (strict) {
                                    throw new FilterValidationException(
                                        "Operador 'gte' só pode ser usado em campos numéricos"
                                    );
                                }
                                break;
                            case "lte":
                                if (isNumericType(entityField.getType())) {
                                    predicates.add(cb.le(root.get(fieldName), (Number) convertValue(value, entityField.getType())));
                                } else if (strict) {
                                    throw new FilterValidationException(
                                        "Operador 'lte' só pode ser usado em campos numéricos"
                                    );
                                }
                                break;
                            default:
                                break;
                        }
                    } catch (NoSuchFieldException e) {
                        if (strict) {
                            throw new FilterValidationException(
                                "Campo '" + field.split("__")[0] + "' não encontrado na entidade " + entityClass.getSimpleName()
                            );
                        }
                        // In lenient mode, silently ignore
                    }
                });
            }
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private void validateFilters(Map<String, String> filters) {
        if (filters == null) return;
        
        filters.forEach((field, value) -> {
            String[] parts = field.split("__", 2);
            String fieldName = parts[0];
            String operation = parts.length > 1
                    ? parts[1].toLowerCase(Locale.ROOT)
                    : "eq";
            
            // Validate operation
            if (!Set.of("eq", "like", "gt", "lt", "gte", "lte").contains(operation)) {
                if (strict) {
                    throw new FilterValidationException(
                        "Operador '" + operation + "' não suportado. Operadores válidos: eq, like, gt, lt, gte, lte"
                    );
                }
                return;
            }
            
            // Validate field is allowed
            if (!allowedFields.isEmpty() && !allowedFields.contains(fieldName)) {
                if (strict) {
                    throw new FilterValidationException(
                        "Campo '" + fieldName + "' não é permitido para filtro. Campos permitidos: " + allowedFields
                    );
                }
                return;
            }
            
            // Validate field exists in entity
            try {
                Field entityField = findField(entityClass, fieldName);
                entityField.setAccessible(true);
                
                // Validate operator compatibility with field type
                switch (operation) {
                    case "like":
                        if (entityField.getType() != String.class && strict) {
                            throw new FilterValidationException(
                                "Operador 'like' só pode ser usado em campos do tipo String"
                            );
                        }
                        break;
                    case "gt":
                    case "lt":
                    case "gte":
                    case "lte":
                        if (!isNumericType(entityField.getType()) && strict) {
                            throw new FilterValidationException(
                                "Operador '" + operation + "' só pode ser usado em campos numéricos"
                            );
                        }
                        break;
                }
            } catch (NoSuchFieldException e) {
                if (strict) {
                    throw new FilterValidationException(
                        "Campo '" + fieldName + "' não encontrado na entidade " + entityClass.getSimpleName()
                    );
                }
            }
        });
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
}
