package filter;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SpecificationBuilder {

    private static final Set<String> PARAMS_RESERVADOS = Set.of("page", "size", "sort");

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
                }
            });

            return builder.and(predicates.toArray(new Predicate[0]));
        };
    }
}