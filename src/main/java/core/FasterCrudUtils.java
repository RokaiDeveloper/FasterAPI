package core;

import annotation.FasterCRUD;
import java.util.Locale;

public final class FasterCrudUtils {

    private FasterCrudUtils() {}

    /**
     * Obtém o nome do recurso a partir da anotação @FasterCRUD.
     * Se path estiver preenchido, retorna path.
     * Caso contrário, retorna o nome simples da classe em minúsculo.
     */
    public static String getResourceName(Class<?> entityClass) {
        FasterCRUD annotation = entityClass.getAnnotation(FasterCRUD.class);
        if (annotation != null && !annotation.path().isBlank()) {
            return annotation.path().trim();
        }
        return entityClass.getSimpleName().toLowerCase(Locale.ROOT);
    }

    /**
     * Retorna o path completo (com barra inicial) para uso em rotas.
     */
    public static String getPath(Class<?> entityClass) {
        String name = getResourceName(entityClass);
        return name.startsWith("/") ? name : "/" + name;
    }
}