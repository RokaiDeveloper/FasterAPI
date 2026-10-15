package com.rokaidev.fasterapi.autoconfigure;

import com.rokaidev.fasterapi.web.FasterFilterArgumentResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
@Order(0)
public class FasterFilterAutoConfiguration implements WebMvcConfigurer {

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(0, new FasterFilterArgumentResolver());
    }
}
