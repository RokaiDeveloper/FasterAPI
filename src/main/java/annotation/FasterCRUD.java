package annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotacao para marcar entidades que devem ter endpoints CRUD gerados automaticamente.
 * Quando aplicada a uma entidade JPA, esta anotacao fara com que o framework
 * FasterAPI crie automaticamente endpoints RESTful para as operacoes basicas.
 * 
 * @author FasterAPI Framework
 * @since 1.0.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface FasterCRUD {
    
    /**
     * Caminho base para os endpoints REST.
     * Exemplo: "produtos" criara endpoints em /api/produtos
     * 
     * @return caminho da API
     */
    String path();
    
    /**
     * Nome customizado para a entidade na API.
     * Se nao for informado, usara o nome da classe em minusculo.
     * 
     * @return nome customizado (opcional)
     */
    String name() default "";
    
    /**
     * Indica se a paginacao deve ser habilitada nos endpoints de listagem.
     * 
     * @return true para habilitar paginacao (default: true)
     */
    boolean pageable() default true;
    
    /**
     * Indica se os filtros dinamicos devem ser habilitados.
     * Quando true, permite filtrar por qualquer campo da entidade via parametros URL.
     * 
     * @return true para habilitar filtros (default: true)
     */
    boolean filterable() default true;
}
