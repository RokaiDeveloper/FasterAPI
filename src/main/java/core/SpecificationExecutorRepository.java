package core;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Interface de repositorio que combina JpaRepository e JpaSpecificationExecutor.
 * Esta interface estende as funcionalidades padrao do Spring Data JPA
 * com suporte a especificacoes para filtragem dinamica.
 * 
 * @param <T> tipo da entidade
 * @author FasterAPI Framework
 * @since 1.0.0
 */
public interface SpecificationExecutorRepository<T>
        extends JpaRepository<T, Long>, JpaSpecificationExecutor<T> {
}