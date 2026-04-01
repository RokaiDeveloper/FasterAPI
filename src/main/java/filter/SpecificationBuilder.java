package filter;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Construtor de especificacoes JPA para filtragem dinamica de dados.
 * Esta classe cria filtros automaticamente baseados nos parametros
 * da requisicao HTTP, permitindo buscas flexiveis.
 * 
 * @author FasterAPI Framework
 * @since 1.0.0
 */
public class SpecificationBuilder {

    /** Parametros reservados que nao devem ser usados como filtros */
    private static final Set<String> PARAMS_RESERVADOS = Set.of("page", "size", "sort");

    /**
     * Constroi uma especificacao JPA a partir dos parametros da requisicao.
     * Cria filtros do tipo LIKE para cada parametro que corresponde a um campo
     * da entidade. Os filtros sao case-insensitive.
     * 
     * @param <T> tipo da entidade
     * @param params mapa com parametros da requisicao
     * @return especificacao JPA para filtragem
     */
    public static <T> Specification<T> construir(Map<String, String> params) {
        return (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();

            params.forEach((campo, valor) -> {
                if (PARAMS_RESERVADOS.contains(campo)) return;

                if (valor == null || valor.isBlank()) return;

                try {
                    root.get(campo);

                    predicates.add(
                            builder.like(
                                    builder.lower(root.get(campo).as(String.class)),
                                    "%" + valor.toLowerCase() + "%"
                            )
                    );
                } catch (IllegalArgumentException e) {
                    // Campo nao existe na entidade, ignora
                }
            });

            return builder.and(predicates.toArray(new Predicate[0]));
        };
    }
}