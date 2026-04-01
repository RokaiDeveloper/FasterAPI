package core;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.util.pattern.PathPatternParser;

import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class GenericCrudController implements InitializingBean {

    private final Class<?> entidade;
    private final String path;
    private GenericCrudService service;

    public void setService(GenericCrudService service) {
        this.service = service;
    }

    private final RequestMappingHandlerMapping handlerMapping;

    public GenericCrudController(Class<?> entidade,
                                 String path,
                                 RequestMappingHandlerMapping handlerMapping) {
        this.entidade = entidade;
        this.path = path;
        this.handlerMapping = handlerMapping;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        registrarRota("findAll", "GET", path);
        registrarRota("findById", "GET", path + "/{id}");
        registrarRota("create", "POST", path);
        registrarRota("update", "PUT", path + "/{id}");
        registrarRota("delete", "DELETE", path + "/{id}");
    }

    private void registrarRota(String nomeDoMetodo,
                               String httpMethod,
                               String urlPattern) throws Exception {

        Method metodo = resolverMetodo(nomeDoMetodo);

        RequestMappingInfo.BuilderConfiguration config = new RequestMappingInfo.BuilderConfiguration();
        config.setPatternParser(new PathPatternParser());

        RequestMappingInfo mappingInfo = RequestMappingInfo
                .paths(urlPattern)
                .methods(org.springframework.web.bind.annotation.RequestMethod.valueOf(httpMethod))
                .options(config)
                .build();

        handlerMapping.registerMapping(mappingInfo, this, metodo);
    }

    private Method resolverMetodo(String nome) throws NoSuchMethodException {
        return switch (nome) {
            case "findAll" -> getClass().getMethod("findAll", HttpServletRequest.class);
            case "findById" -> getClass().getMethod("findById", Long.class);
            case "create" -> getClass().getMethod("create", java.util.Map.class);
            case "update" -> getClass().getMethod("update", Long.class, java.util.Map.class);
            case "delete" -> getClass().getMethod("delete", Long.class);
            default -> throw new IllegalArgumentException("Método desconhecido: " + nome);
        };
    }

    public ResponseEntity<Page<?>> findAll(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        request.getParameterMap().forEach((chave, valores) -> {
            if (valores.length > 0) params.put(chave, valores[0]);
        });

        return ResponseEntity.ok(service.findAll(params));
    }

    public ResponseEntity<?> findById(Long id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    public ResponseEntity<?> create(@RequestBody Map<String, Object> body) {
        Object salvo = service.create(body);
        return ResponseEntity.status(201).body(salvo);
    }

    public ResponseEntity<?> update(@PathVariable Long id,
                                    @RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(service.update(id, body));
    }

    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
