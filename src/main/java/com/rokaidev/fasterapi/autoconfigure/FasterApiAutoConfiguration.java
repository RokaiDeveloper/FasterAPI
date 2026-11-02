package com.rokaidev.fasterapi.autoconfigure;

import jakarta.persistence.EntityManager;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import com.rokaidev.fasterapi.persistence.CrudRegistrationRegistry;
import com.rokaidev.fasterapi.persistence.DtoMapper;
import com.rokaidev.fasterapi.web.CrudDisabledOperationHandler;
import com.rokaidev.fasterapi.web.FilterExceptionHandler;


@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass({EntityManager.class, RequestMappingHandlerMapping.class})
@ConditionalOnMissingBean(FasterCrudInitializer.class)
@EnableConfigurationProperties(FasterApiProperties.class)
@Import({
        FasterCrudInitializer.class,
        CrudRegistrationRegistry.class,
        CrudDisabledOperationHandler.class,
        FilterExceptionHandler.class,
        DtoMapper.class
})
public class FasterApiAutoConfiguration {
}
