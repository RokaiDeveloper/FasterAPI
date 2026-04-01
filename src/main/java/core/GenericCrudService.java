package core;

import jakarta.persistence.EntityManager;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.support.JpaEntityInformationSupport;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import filter.SpecificationBuilder;

public class GenericCrudService {

    private final Class<?> entidade;
    private final SpecificationExecutorRepository<Object> repository;
    private final EntityManager entityManager;

    public GenericCrudService(Class<?> entidade, EntityManager entityManager) {
        this.entidade = entidade;
        this.entityManager = entityManager;

        JpaRepositoryFactory factory = new JpaRepositoryFactory(entityManager);
        this.repository = (SpecificationExecutorRepository<Object>) factory.getRepository(
                SpecificationExecutorRepository.class,
                JpaEntityInformationSupport.getEntityInformation(entidade, entityManager)
        );
    }

    public Page<?> findAll(Map<String, String> params) {
        int page = Integer.parseInt(params.getOrDefault("page", "0"));
        int size = Integer.parseInt(params.getOrDefault("size", "20"));
        String sortField = params.getOrDefault("sort", "id");

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortField));
        var specification = SpecificationBuilder.construir(params);

        return repository.findAll(specification, pageable)
                .map(DtoMapper::converter);
    }

    public Optional<?> findById(Long id) {
        return repository.findById(id)
                .map(DtoMapper::converter);
    }

    @Transactional
    public Object create(Map<String, Object> body) {
        Object instancia = novaInstancia();
        preencherCampos(instancia, body);
        Object salvo = repository.save(instancia);
        return DtoMapper.converter(salvo);
    }

    @Transactional
    public Object update(Long id, Map<String, Object> body) {
        Object instancia = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Não encontrado: " + id));
        preencherCampos(instancia, body);
        Object salvo = repository.save(instancia);
        return DtoMapper.converter(salvo);
    }

    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
    }

    private Object novaInstancia() {
        try {
            return entidade.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Não foi possível instanciar: " + entidade.getSimpleName(), e);
        }
    }

    private void preencherCampos(Object instancia, Map<String, Object> body) {
        body.forEach((nomeCampo, valor) -> {
            try {
                Field campo = entidade.getDeclaredField(nomeCampo);
                campo.setAccessible(true);
                campo.set(instancia, valor);
            } catch (NoSuchFieldException e) {

            } catch (IllegalAccessException e) {
                throw new RuntimeException("Erro ao preencher campo: " + nomeCampo, e);
            }
        });
    }
}