package com.rokaidev.fasterapi.openapi;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Import;

import org.springdoc.core.customizers.OpenApiCustomizer;

@AutoConfiguration
@ConditionalOnClass(OpenApiCustomizer.class)
@ConditionalOnProperty(
        prefix = "fasterapi.openapi",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true)
@Import(OpenApiConfig.class)
public class FasterApiOpenApiAutoConfiguration {
}
