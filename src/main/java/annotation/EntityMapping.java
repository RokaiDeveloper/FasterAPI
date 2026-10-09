package annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation para mapear um DTO para uma entidade JPA.
 * Usada quando @FasterCRUD é aplicada diretamente em um DTO.
 * 
 * Exemplo:
 * <pre>
 * {@code
 * @EntityMapping(entity = Produto.class)
 * public class ProdutoDTO {
 *     private Long id;
 *     private String nome;
 *     private BigDecimal preco;
 *     // getters e setters
 * }
 * }
 * </pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface EntityMapping {
    /**
     * A classe da entidade JPA alvo do mapeamento.
     */
    Class<?> entity();
}
