package annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface FasterCRUD {

    /**
     * Caminho base para os endpoints REST.
     * Se vazio, será usado o nome da classe em minúsculo.
     */
    String path() default "";
}