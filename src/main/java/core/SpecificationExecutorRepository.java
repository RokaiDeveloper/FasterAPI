package core;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SpecificationExecutorRepository<T>
        extends JpaRepository<T, Long>, JpaSpecificationExecutor<T> {
}