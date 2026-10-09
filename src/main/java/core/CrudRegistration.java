package core;

import java.util.Set;

public record CrudRegistration(
        String path,
        Class<?> apiClass,
        Class<?> entityClass,
        boolean dto,
        Set<CrudOperation> operations) {

    public boolean supports(CrudOperation operation) {
        return operations.contains(operation);
    }
}
