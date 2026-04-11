package core;

import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class GenericCrudService<T> {

    private final SimpleJpaRepository<T, Long> repository;
    private final Class<T> entityClass;
    private final TransactionTemplate transactionTemplate;

    public GenericCrudService(SimpleJpaRepository<T, Long> repository,
                              Class<T> entityClass,
                              PlatformTransactionManager transactionManager) {
        this.repository = repository;
        this.entityClass = entityClass;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    public List<T> findAll() {
        return repository.findAll();
    }

    public Optional<T> findById(Long id) {
        return repository.findById(id);
    }

    public T create(Map<String, Object> fields) {
        return transactionTemplate.execute(status -> {
            T entity = newInstance();
            populateFields(entity, fields);
            return repository.save(entity);
        });
    }

    public T update(Long id, Map<String, Object> fields) {
        return transactionTemplate.execute(status -> {
            T existing = repository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Entidade não encontrada com id: " + id));
            populateFields(existing, fields);
            return repository.save(existing);
        });
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
                // Aqui pode ser necessário converter tipos (ex.: String para Enum, Long, etc.)
                field.set(entity, value);
            } catch (NoSuchFieldException e) {
                // Ignora campos que não existem na entidade
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Erro ao definir campo " + fieldName, e);
            }
        });
    }
}