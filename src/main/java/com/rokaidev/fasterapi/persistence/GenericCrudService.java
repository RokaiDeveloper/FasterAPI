package com.rokaidev.fasterapi.persistence;

import com.rokaidev.fasterapi.annotation.ReadOnly;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Locale;
import java.util.stream.Collectors;

public class GenericCrudService<T> {

    private final SimpleJpaRepository<T, Long> repository;
    private final Class<T> entityClass;
    private final TransactionTemplate transactionTemplate;
    private final ObjectMapper objectMapper;
    private final Validator validator;
    private final EntityManager entityManager;
    private final DtoMapper dtoMapper;
    private final Class<?> dtoClass;
    private final boolean useDto;

    public GenericCrudService(SimpleJpaRepository<T, Long> repository,
                              Class<T> entityClass,
                              PlatformTransactionManager transactionManager,
                              Validator validator,
                              EntityManager entityManager) {
        this.repository = repository;
        this.entityClass = entityClass;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.objectMapper = new ObjectMapper();
        this.validator = validator;
        this.entityManager = entityManager;
        this.dtoMapper = null;
        this.dtoClass = null;
        this.useDto = false;
    }

    public GenericCrudService(SimpleJpaRepository<T, Long> repository,
                              Class<T> entityClass,
                              PlatformTransactionManager transactionManager,
                              Validator validator,
                              EntityManager entityManager,
                              DtoMapper dtoMapper,
                              Class<?> dtoClass) {
        this.repository = repository;
        this.entityClass = entityClass;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.objectMapper = new ObjectMapper();
        this.validator = validator;
        this.entityManager = entityManager;
        this.dtoMapper = dtoMapper;
        this.dtoClass = dtoClass;
        this.useDto = dtoClass != null; // Se dtoClass é null, usa Opção 1 (DTO automático)
    }

    @SuppressWarnings("unchecked")
    public List<?> findAll() {
        List<T> entities = repository.findAll();
        if (useDto) {
            return entities.stream()
                    .map(e -> dtoMapper.toDto(e, (Class<Object>) dtoClass))
                    .collect(java.util.stream.Collectors.toList());
        } else {
            // Opção 1: DTO automático - filtra campos com @JsonIgnore
            return entities.stream()
                    .map(dtoMapper::toFilteredDto)
                    .collect(java.util.stream.Collectors.toList());
        }
    }

    @SuppressWarnings("unchecked")
    public org.springframework.data.domain.Page<?> findAll(org.springframework.data.domain.Pageable pageable) {
        org.springframework.data.domain.Page<T> entityPage = repository.findAll(pageable);
        if (useDto) {
            return entityPage.map(e -> dtoMapper.toDto(e, (Class<Object>) dtoClass));
        } else {
            // Opção 1: DTO automático - filtra campos com @JsonIgnore
            return entityPage.map(dtoMapper::toFilteredDto);
        }
    }

    @SuppressWarnings("unchecked")
    public List<?> findAll(Map<String, String> filters) {
        Specification<T> spec = buildSpecification(filters);
        List<T> entities = repository.findAll(spec);
        if (useDto) {
            return entities.stream()
                    .map(e -> dtoMapper.toDto(e, (Class<Object>) dtoClass))
                    .collect(java.util.stream.Collectors.toList());
        } else {
            // Opção 1: DTO automático - filtra campos com @JsonIgnore
            return entities.stream()
                    .map(dtoMapper::toFilteredDto)
                    .collect(java.util.stream.Collectors.toList());
        }
    }

    @SuppressWarnings("unchecked")
    public org.springframework.data.domain.Page<?> findAll(Map<String, String> filters, org.springframework.data.domain.Pageable pageable) {
        Specification<T> spec = buildSpecification(filters);
        org.springframework.data.domain.Page<T> entityPage = repository.findAll(spec, pageable);
        if (useDto) {
            return entityPage.map(e -> dtoMapper.toDto(e, (Class<Object>) dtoClass));
        } else {
            // Opção 1: DTO automático - filtra campos com @JsonIgnore
            return entityPage.map(dtoMapper::toFilteredDto);
        }
    }

    private Specification<T> buildSpecification(Map<String, String> filters) {
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
                        if (!Set.of("eq", "like", "gt", "lt", "gte", "lte").contains(operation)) {
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
                                }
                                break;
                            case "gt":
                                if (isNumericType(entityField.getType())) {
                                    predicates.add(cb.gt(root.get(fieldName), (Number) convertValue(value, entityField.getType())));
                                }
                                break;
                            case "lt":
                                if (isNumericType(entityField.getType())) {
                                    predicates.add(cb.lt(root.get(fieldName), (Number) convertValue(value, entityField.getType())));
                                }
                                break;
                            case "gte":
                                if (isNumericType(entityField.getType())) {
                                    predicates.add(cb.ge(root.get(fieldName), (Number) convertValue(value, entityField.getType())));
                                }
                                break;
                            case "lte":
                                if (isNumericType(entityField.getType())) {
                                    predicates.add(cb.le(root.get(fieldName), (Number) convertValue(value, entityField.getType())));
                                }
                                break;
                            default:
                                break;
                        }
                    } catch (NoSuchFieldException e) {
                        // Ignora campos que não existem
                    }
                });
            }
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };
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

    @SuppressWarnings("unchecked")
    public Optional<?> findById(Long id) {
        Optional<T> entity = repository.findById(id);
        if (useDto && entity.isPresent()) {
            return Optional.of(dtoMapper.toDto(entity.get(), (Class<Object>) dtoClass));
        } else if (entity.isPresent()) {
            // Opção 1: DTO automático - filtra campos com @JsonIgnore
            return Optional.of(dtoMapper.toFilteredDto(entity.get()));
        }
        return entity;
    }

    @SuppressWarnings("unchecked")
    public Object create(Map<String, Object> fields) {
        return transactionTemplate.execute(status -> {
            if (useDto) {
                // Criar DTO a partir dos campos
                Object dto = newInstanceDto();
                populateFields(dto, fields);
                // Mapear DTO para entidade
                T entity = (T) dtoMapper.toEntity(dto, entityClass);
                validate(entity);
                T savedEntity = repository.save(entity);
                // Retornar DTO
                return dtoMapper.toDto(savedEntity, (Class<Object>) dtoClass);
            } else {
                // Opção 1: DTO automático - trabalha com entidade mas retorna DTO filtrado
                T entity = newInstance();
                populateFields(entity, fields);
                validate(entity);
                T savedEntity = repository.save(entity);
                // Retornar DTO filtrado
                return dtoMapper.toFilteredDto(savedEntity);
            }
        });
    }

    @SuppressWarnings("unchecked")
    public Object update(Long id, Map<String, Object> fields) {
        return transactionTemplate.execute(status -> {
            T existing = repository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Entidade não encontrada com id: " + id));
            if (useDto) {
                // Criar DTO a partir dos campos
                Object dto = newInstanceDto();
                populateFields(dto, fields);
                // Atualizar entidade com DTO
                dtoMapper.updateEntity(dto, existing);
                validate(existing);
                T savedEntity = repository.save(existing);
                // Retornar DTO
                return dtoMapper.toDto(savedEntity, (Class<Object>) dtoClass);
            } else {
                // Opção 1: DTO automático - trabalha com entidade mas retorna DTO filtrado
                populateFields(existing, fields);
                validate(existing);
                T savedEntity = repository.save(existing);
                // Retornar DTO filtrado
                return dtoMapper.toFilteredDto(savedEntity);
            }
        });
    }

    @SuppressWarnings("unchecked")
    public Object patch(Long id, Map<String, Object> fields) {
        return transactionTemplate.execute(status -> {
            T existing = repository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Entidade não encontrada com id: " + id));
            if (useDto) {
                // Criar DTO a partir dos campos
                Object dto = newInstanceDto();
                populateFields(dto, fields);
                // Atualizar entidade com DTO
                dtoMapper.updateEntity(dto, existing);
                validate(existing);
                T savedEntity = repository.save(existing);
                // Retornar DTO
                return dtoMapper.toDto(savedEntity, (Class<Object>) dtoClass);
            } else {
                // Opção 1: DTO automático - trabalha com entidade mas retorna DTO filtrado
                populateFields(existing, fields);
                validate(existing);
                T savedEntity = repository.save(existing);
                // Retornar DTO filtrado
                return dtoMapper.toFilteredDto(savedEntity);
            }
        });
    }

    private void validate(T entity) {
        Set<ConstraintViolation<T>> violations = validator.validate(entity);
        if (!violations.isEmpty()) {
            String errorMessage = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(", "));
            throw new ValidationException("Validação falhou: " + errorMessage);
        }
    }

    public void delete(Long id) {
        transactionTemplate.executeWithoutResult(status -> {
            repository.deleteById(id);
        });
    }

    private T newInstance() {
        try {
            var constructor = entityClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao instanciar " + entityClass.getSimpleName(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private Object newInstanceDto() {
        try {
            var constructor = dtoClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao instanciar DTO " + dtoClass.getSimpleName(), e);
        }
    }

    private void populateFields(Object target, Map<String, Object> fields) {
        Class<?> targetClass = target.getClass();
        fields.forEach((fieldName, value) -> {
            try {
                Field field = targetClass.getDeclaredField(fieldName);
                field.setAccessible(true);
                
                // Verifica se o campo é @ReadOnly (apenas para entidades)
                if (targetClass.isAnnotationPresent(jakarta.persistence.Entity.class) && 
                    field.isAnnotationPresent(ReadOnly.class)) {
                    return; // Ignora campos readonly
                }
                
                // Use ObjectMapper para converter o valor para o tipo do campo
                Object convertedValue = objectMapper.convertValue(value, field.getType());
                field.set(target, convertedValue);
            } catch (NoSuchFieldException e) {
                // Ignora campos que não existem na entidade/DTO
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Erro ao definir campo " + fieldName, e);
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Erro de conversão para campo " + fieldName + ": " + e.getMessage(), e);
            }
        });
    }
}