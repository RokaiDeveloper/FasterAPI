package com.rokaidev.fasterapi.autoconfigure;

import com.rokaidev.fasterapi.web.FasterFilterArgumentResolver;
import jakarta.persistence.EntityManager;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
@Order(0)
public class FasterFilterAutoConfiguration implements WebMvcConfigurer {

    private final EntityManager entityManager;

    public FasterFilterAutoConfiguration(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(0, new FasterFilterArgumentResolver(entityManager));
    }
}
