package annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation para gerar automaticamente endpoints CRUD RESTful.
 * Pode ser aplicada em entidades JPA ou DTOs.
 * 
 * Quando aplicada em uma entidade:
 * - Gera endpoints que trabalham diretamente com a entidade
 * - (Futuro) Pode gerar DTO automaticamente em build-time
 * 
 * Quando aplicada em um DTO:
 * - Deve usar @EntityMapping para especificar a entidade alvo
 * - Gera endpoints que trabalham com o DTO, mapeando para a entidade automaticamente
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface FasterCRUD {

    /**
     * Caminho base para os endpoints REST.
     * Se vazio, será usado o nome da classe em minúsculo.
     */
    String path() default "";

    /**
     * Indica se esta classe é um DTO.
     * Se true, deve ser usado com @EntityMapping para especificar a entidade alvo.
     */
    boolean isDto() default false;
}