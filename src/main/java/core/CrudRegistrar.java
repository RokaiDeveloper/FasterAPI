package core;

import annotation.FasterCRUD;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.*;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.util.Set;


public class CrudRegistrar implements BeanDefinitionRegistryPostProcessor {
    private final String basePackage;

    public CrudRegistrar(String basePackage) {
        this.basePackage = basePackage;
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        Set<BeanDefinition> candidatos = encontrarEntidades();

        for(BeanDefinition candidato : candidatos) {
            try {
                Class<?> entidade = Class.forName(candidato.getBeanClassName());
                FasterCRUD config = entidade.getAnnotation(FasterCRUD.class);

                registrarService(registry, entidade, config);
                registrarController(registry, entidade, config);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("Erro ao processar entidade", e);
            }
        }
    }
    private void registrarService(BeanDefinitionRegistry registry,
                                  Class<?> entidade,
                                  FasterCRUD config) {
        GenericBeanDefinition def = new GenericBeanDefinition();
        def.setBeanClass(GenericCrudService.class);
        def.setScope(BeanDefinition.SCOPE_SINGLETON);
        def.getConstructorArgumentValues().addGenericArgumentValue(entidade);
        def.getConstructorArgumentValues().addGenericArgumentValue(
                new RuntimeBeanReference("entityManagerFactory")
        );

        String nomeDoBeanService = resolverNome(config, entidade) + "Service";
        registry.registerBeanDefinition(nomeDoBeanService, def);
    }

    private void registrarController(BeanDefinitionRegistry registry,
                                     Class<?> entidade,
                                     FasterCRUD config) {
        String nomeBase = resolverNome(config, entidade);

        GenericBeanDefinition def = new GenericBeanDefinition();
        def.setBeanClass(GenericCrudController.class);
        def.setScope(BeanDefinition.SCOPE_SINGLETON);
        def.getConstructorArgumentValues().addGenericArgumentValue(entidade);
        def.getConstructorArgumentValues().addGenericArgumentValue(config.path());
        def.getConstructorArgumentValues().addGenericArgumentValue(
                new RuntimeBeanReference("requestMappingHandlerMapping")
        );

        def.getPropertyValues().addPropertyValue(
                "service",
                new RuntimeBeanReference(nomeBase + "Service")
        );

        registry.registerBeanDefinition(nomeBase + "Controller", def);
    }

    private String resolverNome(FasterCRUD config, Class<?> entidade) {
        return config.name().isEmpty()
                ? entidade.getSimpleName().toLowerCase()
                : config.name();
    }

    @Override
    public void postProcessBeanFactory(
            org.springframework.beans.factory.config.ConfigurableListableBeanFactory bf) {
    }

    private Set<BeanDefinition> encontrarEntidades() {
        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false);

        scanner.addIncludeFilter(new AnnotationTypeFilter(FasterCRUD.class));

        return scanner.findCandidateComponents(basePackage);
    }
}
