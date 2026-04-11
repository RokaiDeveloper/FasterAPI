package core;

import annotation.FasterCRUD;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import java.util.HashSet;
import java.util.Set;

@Component
public class FasterCrudInitializer implements ApplicationRunner {

    @Value("${fasterapi.base-package:com.exemplo.entidades}")
    private String basePackage;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private RequestMappingHandlerMapping handlerMapping;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        System.out.println(">>> Escaneando pacote: " + basePackage);
        Set<Class<?>> entities = scanEntities(basePackage);
        System.out.println(">>> Entidades encontradas: " + entities.size());
        entities.forEach(e -> System.out.println("   - " + e.getName()));

        for (Class<?> entityClass : entities) {
            registerCrudForEntity(entityClass);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> void registerCrudForEntity(Class<T> entityClass) {
        FasterCRUD config = entityClass.getAnnotation(FasterCRUD.class);
        String path = config.path().isEmpty()
                ? "/" + entityClass.getSimpleName().toLowerCase()
                : config.path();

        // Garante que o path comece com "/"
        if (!path.startsWith("/")) {
            path = "/" + path;
        }

        // Repositório
        JpaEntityInformation<T, ?> info =
                (JpaEntityInformation<T, ?>) JpaEntityInformationSupport.getEntityInformation(entityClass, entityManager);
        SimpleJpaRepository<T, Long> repository = new SimpleJpaRepository<>(info, entityManager);

        // Serviço (agora passando a classe da entidade)
        GenericCrudService<T> service = new GenericCrudService<>(repository, entityClass, transactionManager);
        // Controller
        GenericCrudController<T> controller = new GenericCrudController<>(service, path);

        String beanName = entityClass.getSimpleName().toLowerCase() + "Controller";
        applicationContext.getBeanFactory().registerSingleton(beanName, controller);

        // Registra os mapeamentos
        registerControllerMappings(controller, path);
    }

    private void registerControllerMappings(Object controller, String basePath) {
        for (Method method : controller.getClass().getDeclaredMethods()) {
            String subPath = "";
            RequestMethod httpMethod = null;

            if (method.isAnnotationPresent(GetMapping.class)) {
                GetMapping annotation = method.getAnnotation(GetMapping.class);
                subPath = annotation.value().length > 0 ? annotation.value()[0] : "";
                httpMethod = RequestMethod.GET;
            } else if (method.isAnnotationPresent(PostMapping.class)) {
                httpMethod = RequestMethod.POST;
            } else if (method.isAnnotationPresent(PutMapping.class)) {
                PutMapping annotation = method.getAnnotation(PutMapping.class);
                subPath = annotation.value().length > 0 ? annotation.value()[0] : "";
                httpMethod = RequestMethod.PUT;
            } else if (method.isAnnotationPresent(DeleteMapping.class)) {
                DeleteMapping annotation = method.getAnnotation(DeleteMapping.class);
                subPath = annotation.value().length > 0 ? annotation.value()[0] : "";
                httpMethod = RequestMethod.DELETE;
            }

            if (httpMethod != null) {
                String fullPath = basePath + subPath;
                RequestMappingInfo mappingInfo = RequestMappingInfo
                        .paths(fullPath)
                        .methods(httpMethod)
                        .build();
                handlerMapping.registerMapping(mappingInfo, controller, method);
            }
        }
    }

    private Set<Class<?>> scanEntities(String basePackage) throws Exception {
        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(FasterCRUD.class));

        Set<Class<?>> classes = new HashSet<>();
        for (BeanDefinition bd : scanner.findCandidateComponents(basePackage)) {
            classes.add(Class.forName(bd.getBeanClassName()));
        }
        return classes;
    }
}