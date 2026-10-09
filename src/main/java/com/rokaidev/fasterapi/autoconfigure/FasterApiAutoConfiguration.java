package com.rokaidev.fasterapi.autoconfigure;

import com.rokaidev.fasterapi.persistence.CrudRegistrationRegistry;
import com.rokaidev.fasterapi.persistence.DtoMapper;
import com.rokaidev.fasterapi.web.CrudDisabledOperationHandler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Import;

import jakarta.persistence.EntityManager;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass({EntityManager.class, RequestMappingHandlerMapping.class})
@ConditionalOnMissingBean(FasterCrudInitializer.class)
@Import({
        FasterCrudInitializer.class,
        CrudRegistrationRegistry.class,
        CrudDisabledOperationHandler.class,
        DtoMapper.class
})
public class FasterApiAutoConfiguration {
}
