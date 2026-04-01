package annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotacao para definir uma classe DTO (Data Transfer Object) para uma entidade.
 * Quando aplicada a uma entidade junto com @FasterCRUD, as respostas da API
 * serao convertidas automaticamente para o DTO especificado.
 * 
 * @author FasterAPI Framework
 * @since 1.0.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface CrudDTO {
    
    /**
     * Classe DTO que sera usada para converter as respostas da API.
     * A classe DTO deve ter um construtor padrao sem argumentos.
     * 
     * @return classe DTO para conversao
     */
    Class<?> value();
}