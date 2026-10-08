package core;

import annotation.ReadOnly;
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
import java.util.stream.Collectors;

public class GenericCrudService<T> {

    private final SimpleJpaRepository<T, Long> repository;
    private final Class<T> entityClass;
    private final TransactionTemplate transactionTemplate;
    private final ObjectMapper objectMapper;
    private final Validator validator;
    private final EntityManager entityManager;

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
    }

    public List<T> findAll() {
        return repository.findAll();
    }

    public org.springframework.data.domain.Page<T> findAll(org.springframework.data.domain.Pageable pageable) {
        return repository.findAll(pageable);
    }

    public List<T> findAll(Map<String, String> filters) {
        Specification<T> spec = buildSpecification(filters);
        return repository.findAll(spec);
    }

    public org.springframework.data.domain.Page<T> findAll(Map<String, String> filters, org.springframework.data.domain.Pageable pageable) {
        Specification<T> spec = buildSpecification(filters);
        return repository.findAll(spec, pageable);
    }

    private Specification<T> buildSpecification(Map<String, String> filters) {
        return (Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (filters != null) {
                filters.forEach((field, value) -> {
                    try {
                        Field entityField = entityClass.getDeclaredField(field);
                        entityField.setAccessible(true);
                        
                        // Suporte a operadores: field__op=value
                        String[] parts = field.split("__");
                        String fieldName = parts[0];
                        String operation = parts.length > 1 ? parts[1] : "eq";
                        
                        switch (operation) {
                            case "eq":
                                predicates.add(cb.equal(root.get(fieldName), convertValue(value, entityField.getType())));
                                break;
                            case "like":
                                predicates.add(cb.like(root.get(fieldName), "%" + value + "%"));
                                break;
                            case "gt":
                                if (Number.class.isAssignableFrom(entityField.getType())) {
                                    predicates.add(cb.gt(root.get(fieldName), (Number) convertValue(value, entityField.getType())));
                                }
                                break;
                            case "lt":
                                if (Number.class.isAssignableFrom(entityField.getType())) {
                                    predicates.add(cb.lt(root.get(fieldName), (Number) convertValue(value, entityField.getType())));
                                }
                                break;
                            case "gte":
                                if (Number.class.isAssignableFrom(entityField.getType())) {
                                    predicates.add(cb.ge(root.get(fieldName), (Number) convertValue(value, entityField.getType())));
                                }
                                break;
                            case "lte":
                                if (Number.class.isAssignableFrom(entityField.getType())) {
                                    predicates.add(cb.le(root.get(fieldName), (Number) convertValue(value, entityField.getType())));
                                }
                                break;
                            default:
                                predicates.add(cb.equal(root.get(fieldName), convertValue(value, entityField.getType())));
                        }
                    } catch (NoSuchFieldException e) {
                        // Ignora campos que não existem
                    }
                });
            }
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Object convertValue(String value, Class<?> targetType) {
        return objectMapper.convertValue(value, targetType);
    }

    public Optional<T> findById(Long id) {
        return repository.findById(id);
    }

    public T create(Map<String, Object> fields) {
        return transactionTemplate.execute(status -> {
            T entity = newInstance();
            populateFields(entity, fields);
            validate(entity);
            return repository.save(entity);
        });
    }

    public T update(Long id, Map<String, Object> fields) {
        return transactionTemplate.execute(status -> {
            T existing = repository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Entidade não encontrada com id: " + id));
            populateFields(existing, fields);
            validate(existing);
            return repository.save(existing);
        });
    }

    public T patch(Long id, Map<String, Object> fields) {
        return transactionTemplate.execute(status -> {
            T existing = repository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Entidade não encontrada com id: " + id));
            populateFields(existing, fields);
            validate(existing);
            return repository.save(existing);
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
            return entityClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao instanciar " + entityClass.getSimpleName(), e);
        }
    }

    private void populateFields(T entity, Map<String, Object> fields) {
        fields.forEach((fieldName, value) -> {
            try {
                Field field = entityClass.getDeclaredField(fieldName);
                field.setAccessible(true);
                
                // Verifica se o campo é @ReadOnly
                if (field.isAnnotationPresent(ReadOnly.class)) {
                    return; // Ignora campos readonly
                }
                
                // Use ObjectMapper para converter o valor para o tipo do campo
                Object convertedValue = objectMapper.convertValue(value, field.getType());
                field.set(entity, convertedValue);
            } catch (NoSuchFieldException e) {
                // Ignora campos que não existem na entidade
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Erro ao definir campo " + fieldName, e);
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Erro de conversão para campo " + fieldName + ": " + e.getMessage(), e);
            }
        });
    }
}