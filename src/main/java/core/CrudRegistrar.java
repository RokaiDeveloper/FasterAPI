package core;

import annotation.FasterCRUD;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.*;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.util.Set;

/**
 * Registrador de beans CRUD para o framework FasterAPI.
 * Esta classe escaneia o classpath em busca de entidades anotadas com @FasterCRUD
 * e registra automaticamente os beans de Service e Controller necessarios.
 * 
 * @author FasterAPI Framework
 * @since 1.0.0
 */
public class CrudRegistrar implements BeanDefinitionRegistryPostProcessor {
    
    /** Pacote base para escaneamento de entidades */
    private final String basePackage;

    /**
     * Construtor do registrador CRUD.
     * 
     * @param basePackage pacote base onde procurar entidades anotadas
     */
    public CrudRegistrar(String basePackage) {
        this.basePackage = basePackage;
    }

    /**
     * Processa o registro de beans apos a inicializacao do contexto.
     * Escaneia as entidades e registra os beans necessarios.
     * 
     * @param registry registro de beans do Spring
     * @throws BeansException em caso de erro no processamento
     */
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
    
    /**
     * Registra o bean de Service para uma entidade.
     * 
     * @param registry registro de beans do Spring
     * @param entidade classe da entidade
     * @param config configuracao da anotacao @FasterCRUD
     */
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

    /**
     * Registra o bean de Controller para uma entidade.
     * 
     * @param registry registro de beans do Spring
     * @param entidade classe da entidade
     * @param config configuracao da anotacao @FasterCRUD
     */
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

    /**
     * Resolve o nome base para os beans a partir da configuracao.
     * 
     * @param config configuracao da anotacao @FasterCRUD
     * @param entidade classe da entidade
     * @return nome base para os beans
     */
    private String resolverNome(FasterCRUD config, Class<?> entidade) {
        return config.name().isEmpty()
                ? entidade.getSimpleName().toLowerCase()
                : config.name();
    }

    /**
     * Metodo da interface BeanDefinitionRegistryPostProcessor.
     * Nao utilizado neste implementacao.
     * 
     * @param bf fabrica de beans configuravel
     */
    @Override
    public void postProcessBeanFactory(
            org.springframework.beans.factory.config.ConfigurableListableBeanFactory bf) {
    }

    /**
     * Escaneia o classpath em busca de entidades anotadas com @FasterCRUD.
     * 
     * @return conjunto de definicoes de beans das entidades encontradas
     */
    private Set<BeanDefinition> encontrarEntidades() {
        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false);

        scanner.addIncludeFilter(new AnnotationTypeFilter(FasterCRUD.class));

        return scanner.findCandidateComponents(basePackage);
    }
}
