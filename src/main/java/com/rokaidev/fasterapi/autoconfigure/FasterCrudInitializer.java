package com.rokaidev.fasterapi.autoconfigure;

import com.rokaidev.fasterapi.annotation.EntityMapping;
import com.rokaidev.fasterapi.annotation.FasterCRUD;
import com.rokaidev.fasterapi.persistence.CrudOperation;
import com.rokaidev.fasterapi.persistence.CrudRegistration;
import com.rokaidev.fasterapi.persistence.CrudRegistrationRegistry;
import com.rokaidev.fasterapi.persistence.DtoMapper;
import com.rokaidev.fasterapi.persistence.GenericCrudService;
import com.rokaidev.fasterapi.web.GenericCrudController;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Entity;
import jakarta.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.JpaEntityInformationSupport;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class FasterCrudInitializer implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(FasterCrudInitializer.class);

    @Autowired
    private FasterApiProperties properties;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private RequestMappingHandlerMapping handlerMapping;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    private Validator validator;

    @Autowired
    private DtoMapper dtoMapper;

    @Autowired
    private CrudRegistrationRegistry registrationRegistry;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        List<String> scanPackages = properties.getScanPackages();
        if (scanPackages.isEmpty()) {
            logger.warn(">>> Nenhum pacote FasterAPI configurado; nenhum CRUD será registrado. " +
                    "Configure fasterapi.base-package, fasterapi.base-packages, " +
                    "fasterapi.model-packages ou fasterapi.dto-packages.");
            return;
        }
        logger.info(">>> Escaneando pacotes: {}", scanPackages);
        Set<Class<?>> classes = scanEntities(scanPackages);
        logger.info(">>> Classes encontradas: {}", classes.size());
        classes.forEach(e -> logger.info("   - {}", e.getName()));
        if (classes.isEmpty()) {
            logger.warn(">>> Nenhuma classe anotada com @FasterCRUD foi encontrada nos pacotes: {}",
                    scanPackages);
            return;
        }

        validateUniquePaths(classes);
        for (Class<?> clazz : classes) {
            registerCrudForClass(clazz);
        }
    }

    private void validateUniquePaths(Set<Class<?>> classes) {
        Map<String, Class<?>> paths = new HashMap<>();
        for (Class<?> clazz : classes) {
            FasterCRUD config = clazz.getAnnotation(FasterCRUD.class);
            String path = resolvePath(clazz, config);
            Class<?> previous = paths.putIfAbsent(path, clazz);
            if (previous != null) {
                throw new IllegalStateException("Path CRUD duplicado '" + path + "' para "
                        + previous.getName() + " e " + clazz.getName()
                        + ". Defina paths diferentes em @FasterCRUD.");
            }
        }
    }

    @SuppressWarnings("unchecked")
    private <T> void registerCrudForClass(Class<T> clazz) {
        FasterCRUD config = clazz.getAnnotation(FasterCRUD.class);
        String path = resolvePath(clazz, config);

        boolean isDto = config.isDto();
        Class<?> entityClass;
        
        if (isDto) {
            // Se for DTO, obter a entidade alvo via @EntityMapping
            EntityMapping entityMapping = clazz.getAnnotation(EntityMapping.class);
            if (entityMapping == null) {
                throw new IllegalArgumentException("DTO " + clazz.getName() + " deve ter @EntityMapping annotation");
            }
            entityClass = entityMapping.entity();
            logger.info(">>> Registrando DTO: {} -> Entidade: {}", clazz.getSimpleName(), entityClass.getSimpleName());
        } else {
            // Se for entidade, usar diretamente
            entityClass = clazz;
            logger.info(">>> Registrando Entidade: {}", entityClass.getSimpleName());
        }

        JpaEntityInformation<?, ?> info =
                JpaEntityInformationSupport.getEntityInformation(entityClass, entityManager);
        SimpleJpaRepository<?, Long> repository = new SimpleJpaRepository<>(info, entityManager);

        @SuppressWarnings("unchecked")
        GenericCrudService<?> service;
        if (isDto) {
            service = new GenericCrudService<Object>((SimpleJpaRepository<Object, Long>) repository, 
                    (Class<Object>) entityClass, transactionManager, validator, entityManager, dtoMapper, clazz);
        } else {
            // Opção 1: DTO automático - usa dtoMapper para filtrar campos com @JsonIgnore
            service = new GenericCrudService<Object>((SimpleJpaRepository<Object, Long>) repository, 
                    (Class<Object>) entityClass, transactionManager, validator, entityManager, dtoMapper, null);
        }
        
        GenericCrudController<?> controller = new GenericCrudController<>(
                service,
                path,
                config.enableGet(),
                config.enablePost(),
                config.enablePut(),
                config.enablePatch(),
                config.enableDelete());

        String beanName = clazz.getSimpleName().toLowerCase() + "Controller";
        applicationContext.getBeanFactory().registerSingleton(beanName, controller);

        // Registra os mapeamentos
        Set<CrudOperation> operations = EnumSet.noneOf(CrudOperation.class);
        if (config.enableGet()) {
            operations.add(CrudOperation.GET_LIST);
            operations.add(CrudOperation.GET_ONE);
        }
        if (config.enablePost()) {
            operations.add(CrudOperation.POST);
        }
        if (config.enablePut()) {
            operations.add(CrudOperation.PUT);
        }
        if (config.enablePatch()) {
            operations.add(CrudOperation.PATCH);
        }
        if (config.enableDelete()) {
            operations.add(CrudOperation.DELETE);
        }

        registerControllerMappings(controller, path, operations);
        registrationRegistry.add(new CrudRegistration(path, clazz, entityClass, isDto, operations));
        
        logger.info(">>> CRUD registrado para: {} com path: {}", clazz.getSimpleName(), path);
    }

    private String resolvePath(Class<?> clazz, FasterCRUD config) {
        String path = config.path().isEmpty()
                ? "/" + clazz.getSimpleName().toLowerCase()
                : config.path().trim();
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        while (path.length() > 1 && path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        return path;
    }

    private void registerControllerMappings(Object controller, String basePath, Set<CrudOperation> operations) {
        for (Method method : controller.getClass().getDeclaredMethods()) {
            String subPath = "";
            RequestMethod httpMethod = null;
            CrudOperation operation = null;

            if (method.isAnnotationPresent(GetMapping.class)) {
                GetMapping annotation = method.getAnnotation(GetMapping.class);
                subPath = annotation.value().length > 0 ? annotation.value()[0] : "";
                httpMethod = RequestMethod.GET;
                operation = subPath.isEmpty() ? CrudOperation.GET_LIST : CrudOperation.GET_ONE;
            } else if (method.isAnnotationPresent(PostMapping.class)) {
                httpMethod = RequestMethod.POST;
                operation = CrudOperation.POST;
            } else if (method.isAnnotationPresent(PutMapping.class)) {
                PutMapping annotation = method.getAnnotation(PutMapping.class);
                subPath = annotation.value().length > 0 ? annotation.value()[0] : "";
                httpMethod = RequestMethod.PUT;
                operation = CrudOperation.PUT;
            } else if (method.isAnnotationPresent(DeleteMapping.class)) {
                DeleteMapping annotation = method.getAnnotation(DeleteMapping.class);
                subPath = annotation.value().length > 0 ? annotation.value()[0] : "";
                httpMethod = RequestMethod.DELETE;
                operation = CrudOperation.DELETE;
            } else if (method.isAnnotationPresent(PatchMapping.class)) {
                PatchMapping annotation = method.getAnnotation(PatchMapping.class);
                subPath = annotation.value().length > 0 ? annotation.value()[0] : "";
                httpMethod = RequestMethod.PATCH;
                operation = CrudOperation.PATCH;
            }

            if (httpMethod != null && operation != null && operations.contains(operation)) {
                String fullPath = basePath + subPath;
                RequestMappingInfo mappingInfo = RequestMappingInfo
                        .paths(fullPath)
                        .methods(httpMethod)
                        .build();
                handlerMapping.registerMapping(mappingInfo, controller, method);
                logger.debug(">>> Mapeamento registrado: {} {} -> {}", httpMethod, fullPath, method.getName());
            }
        }
    }

    private Set<Class<?>> scanEntities(List<String> basePackages) throws Exception {
        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(FasterCRUD.class));

        Set<Class<?>> classes = new LinkedHashSet<>();
        for (String basePackage : basePackages) {
            for (BeanDefinition bd : scanner.findCandidateComponents(basePackage)) {
                classes.add(Class.forName(bd.getBeanClassName()));
            }
        }
        return classes;
    }
}