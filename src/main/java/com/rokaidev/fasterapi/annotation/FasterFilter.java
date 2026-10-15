package com.rokaidev.fasterapi.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface FasterFilter {
    
    /**
     * Entity class to validate filter fields against
     */
    Class<?> entity();
    
    /**
     * Allowed field names for filtering
     */
    String[] fields();
    
    /**
     * If true, throws exception for invalid/unknown filters
     * If false, silently ignores invalid filters
     * Default: true
     */
    boolean strict() default true;
}
