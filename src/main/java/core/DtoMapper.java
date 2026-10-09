package core;

import annotation.ReadOnly;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Classe utilitária para mapeamento entre DTOs e Entidades.
 * Usa reflection para mapear campos com o mesmo nome.
 */
@Component
public class DtoMapper {

    private final ObjectMapper objectMapper;
    private final EntityManager entityManager;

    public DtoMapper(ObjectMapper objectMapper, EntityManager entityManager) {
        this.objectMapper = objectMapper;
        this.entityManager = entityManager;
    }

    /**
     * Converte um DTO para uma entidade.
     */
    public <D, E> E toEntity(D dto, Class<E> entityClass) {
        try {
            E entity = entityClass.getDeclaredConstructor().newInstance();
            mapFields(dto, entity, false);
            return entity;
        } catch (Exception e) {
            throw new RuntimeException("Erro ao converter DTO para entidade", e);
        }
    }

    /**
     * Converte uma entidade para um DTO.
     */
    public <E, D> D toDto(E entity, Class<D> dtoClass) {
        try {
            D dto = dtoClass.getDeclaredConstructor().newInstance();
            mapFields(entity, dto, false);
            return dto;
        } catch (Exception e) {
            throw new RuntimeException("Erro ao converter entidade para DTO", e);
        }
    }

    /**
     * Converte uma entidade para um DTO filtrado (respeita @JsonIgnore).
     * Usado para Opção 1 - geração automática de DTO.
     */
    public <E> Map<String, Object> toFilteredDto(E entity) {
        try {
            Map<String, Object> dto = new HashMap<>();
            Class<?> entityClass = entity.getClass();
            Map<String, Field> fields = getAllFields(entityClass);
            
            for (Map.Entry<String, Field> entry : fields.entrySet()) {
                Field field = entry.getValue();
                field.setAccessible(true);
                
                // Ignora campos com @JsonIgnore
                if (field.isAnnotationPresent(JsonIgnore.class)) {
                    continue;
                }
                
                Object value = field.get(entity);
                if (value != null) {
                    dto.put(entry.getKey(), value);
                }
            }
            
            return dto;
        } catch (Exception e) {
            throw new RuntimeException("Erro ao converter entidade para DTO filtrado", e);
        }
    }

    /**
     * Atualiza uma entidade existente com dados de um DTO.
     */
    public <D, E> void updateEntity(D dto, E entity) {
        try {
            mapFields(dto, entity, false);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao atualizar entidade com DTO", e);
        }
    }

    /**
     * Mapeia campos de um objeto para outro usando reflection.
     * Copia campos com o mesmo nome e tipo compatível.
     */
    private void mapFields(Object source, Object target, boolean filterJsonIgnore) throws Exception {
        Class<?> sourceClass = source.getClass();
        Class<?> targetClass = target.getClass();

        Map<String, Field> sourceFields = getAllFields(sourceClass);
        Map<String, Field> targetFields = getAllFields(targetClass);

        for (Map.Entry<String, Field> entry : sourceFields.entrySet()) {
            String fieldName = entry.getKey();
            Field sourceField = entry.getValue();
            Field targetField = targetFields.get(fieldName);

            if (targetField != null && isTypeCompatible(sourceField.getType(), targetField.getType())) {
                // Se estiver filtrando @JsonIgnore e o campo alvo tiver, ignora
                if (filterJsonIgnore && targetField.isAnnotationPresent(JsonIgnore.class)) {
                    continue;
                }
                
                sourceField.setAccessible(true);
                targetField.setAccessible(true);
                Object value = sourceField.get(source);
                if (value != null) {
                    targetField.set(target, value);
                }
            }
        }
    }

    /**
     * Obtém todos os campos de uma classe (incluindo herança).
     */
    private Map<String, Field> getAllFields(Class<?> clazz) {
        Map<String, Field> fields = new HashMap<>();
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            for (Field field : current.getDeclaredFields()) {
                if (!fields.containsKey(field.getName())) {
                    fields.put(field.getName(), field);
                }
            }
            current = current.getSuperclass();
        }
        return fields;
    }

    /**
     * Verifica se os tipos são compatíveis para mapeamento.
     */
    private boolean isTypeCompatible(Class<?> sourceType, Class<?> targetType) {
        if (sourceType.equals(targetType)) {
            return true;
        }
        
        // Permite mapeamento entre tipos numéricos
        if (Number.class.isAssignableFrom(sourceType) && Number.class.isAssignableFrom(targetType)) {
            return true;
        }
        
        // Permite mapeamento String para String
        if (sourceType.equals(String.class) && targetType.equals(String.class)) {
            return true;
        }
        
        // Permite mapeamento de primitivos para wrappers
        if (isPrimitiveWrapperMatch(sourceType, targetType)) {
            return true;
        }
        
        return false;
    }

    /**
     * Verifica se é um match entre primitivo e wrapper.
     */
    private boolean isPrimitiveWrapperMatch(Class<?> type1, Class<?> type2) {
        return (type1 == int.class && type2 == Integer.class) ||
               (type1 == Integer.class && type2 == int.class) ||
               (type1 == long.class && type2 == Long.class) ||
               (type1 == Long.class && type2 == long.class) ||
               (type1 == double.class && type2 == Double.class) ||
               (type1 == Double.class && type2 == double.class) ||
               (type1 == float.class && type2 == Float.class) ||
               (type1 == Float.class && type2 == float.class) ||
               (type1 == boolean.class && type2 == Boolean.class) ||
               (type1 == Boolean.class && type2 == boolean.class);
    }
}
