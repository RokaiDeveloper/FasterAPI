package core;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("FasterAPI")
                        .version("1.0")
                        .description("Mini framework para automatizar criação de CRUDs RESTful"));
    }

    @Bean
    public OpenApiCustomizer crudOpenApiCustomizer(CrudRegistrationRegistry registry) {
        return openAPI -> {
            if (openAPI.getPaths() == null) {
                openAPI.setPaths(new Paths());
            }
            if (openAPI.getComponents() == null) {
                openAPI.setComponents(new Components());
            }

            for (CrudRegistration registration : registry.getRegistrations()) {
                PathItem collectionPath = openAPI.getPaths()
                        .computeIfAbsent(registration.path(), ignored -> new PathItem());
                PathItem itemPath = openAPI.getPaths()
                        .computeIfAbsent(registration.path() + "/{id}", ignored -> new PathItem());
                String tag = registration.apiClass().getSimpleName();
                Schema<?> schema = schemaFor(registration);
                String schemaName = tag + "Response";
                openAPI.getComponents().addSchemas(schemaName, schema);

                if (registration.supports(CrudOperation.GET_LIST)) {
                    Operation operation = operation("Listar " + tag, tag);
                    operation.addParametersItem(queryParameter("page", "Página, iniciando em 0", new IntegerSchema()));
                    operation.addParametersItem(queryParameter("size", "Quantidade de registros por página", new IntegerSchema()));
                    operation.addParametersItem(queryParameter("sort", "Ordenação no formato campo,direção", new StringSchema()));
                    operation.setResponses(responses("200", new ArraySchema().items(ref(schemaName))));
                    addFilterParameters(operation, registration);
                    collectionPath.setGet(operation);
                }
                if (registration.supports(CrudOperation.GET_ONE)) {
                    Operation operation = operation("Buscar " + tag + " por ID", tag);
                    operation.addParametersItem(idParameter());
                    operation.setResponses(responses("200", ref(schemaName), "404", null));
                    itemPath.setGet(operation);
                }
                if (registration.supports(CrudOperation.POST)) {
                    Operation operation = operation("Criar " + tag, tag);
                    operation.setRequestBody(requestBody(schemaName));
                    operation.setResponses(responses("201", ref(schemaName), "400", null));
                    collectionPath.setPost(operation);
                }
                if (registration.supports(CrudOperation.PUT)) {
                    Operation operation = operation("Atualizar " + tag, tag);
                    operation.addParametersItem(idParameter());
                    operation.setRequestBody(requestBody(schemaName));
                    operation.setResponses(responses("200", ref(schemaName), "400", null, "404", null));
                    itemPath.setPut(operation);
                }
                if (registration.supports(CrudOperation.PATCH)) {
                    Operation operation = operation("Atualizar parcialmente " + tag, tag);
                    operation.addParametersItem(idParameter());
                    operation.setRequestBody(requestBody(schemaName));
                    operation.setResponses(responses("200", ref(schemaName), "400", null, "404", null));
                    itemPath.setPatch(operation);
                }
                if (registration.supports(CrudOperation.DELETE)) {
                    Operation operation = operation("Excluir " + tag, tag);
                    operation.addParametersItem(idParameter());
                    operation.setResponses(responses("204", null, "404", null));
                    itemPath.setDelete(operation);
                }
                if (isEmpty(collectionPath)) {
                    openAPI.getPaths().remove(registration.path());
                }
                if (isEmpty(itemPath)) {
                    openAPI.getPaths().remove(registration.path() + "/{id}");
                }
            }
        };
    }

    private boolean isEmpty(PathItem pathItem) {
        return pathItem.getGet() == null
                && pathItem.getPost() == null
                && pathItem.getPut() == null
                && pathItem.getPatch() == null
                && pathItem.getDelete() == null;
    }

    private Operation operation(String summary, String tag) {
        return new Operation().summary(summary).addTagsItem(tag);
    }

    private Parameter idParameter() {
        return new Parameter().name("id").in("path").required(true).schema(new IntegerSchema().format("int64"));
    }

    private Parameter queryParameter(String name, String description, Schema<?> schema) {
        return new Parameter().name(name).in("query").description(description).required(false).schema(schema);
    }

    private void addFilterParameters(Operation operation, CrudRegistration registration) {
        for (Field field : allFields(registration.entityClass()).values()) {
            if (field.isSynthetic() || field.isAnnotationPresent(JsonIgnore.class)
                    || field.getName().equals("id")) {
                continue;
            }
            Schema<?> schema = new Schema<>().type(typeName(field.getType()));
            operation.addParametersItem(queryParameter(field.getName(), "Filtro por igualdade", schema));
            operation.addParametersItem(queryParameter(field.getName() + "__like", "Filtro LIKE", new StringSchema()));
            operation.addParametersItem(queryParameter(field.getName() + "__eq", "Filtro de igualdade", schema));
            operation.addParametersItem(queryParameter(field.getName() + "__gt", "Filtro maior que", schema));
            operation.addParametersItem(queryParameter(field.getName() + "__lt", "Filtro menor que", schema));
            operation.addParametersItem(queryParameter(field.getName() + "__gte", "Filtro maior ou igual", schema));
            operation.addParametersItem(queryParameter(field.getName() + "__lte", "Filtro menor ou igual", schema));
        }
    }

    private RequestBody requestBody(String schemaName) {
        return new RequestBody().required(true)
                .content(new Content().addMediaType("application/json",
                        new io.swagger.v3.oas.models.media.MediaType().schema(ref(schemaName))));
    }

    private ApiResponses responses(Object... values) {
        ApiResponses responses = new ApiResponses();
        for (int i = 0; i < values.length; i += 2) {
            String code = (String) values[i];
            Schema<?> schema = i + 1 < values.length && values[i + 1] instanceof Schema<?> value
                    ? value : null;
            ApiResponse response = new ApiResponse().description(description(code));
            if (schema != null) {
                response.setContent(new Content().addMediaType("application/json",
                        new io.swagger.v3.oas.models.media.MediaType().schema(schema)));
            }
            responses.addApiResponse(code, response);
        }
        return responses;
    }

    private String description(String code) {
        return switch (code) {
            case "200" -> "Operação realizada com sucesso";
            case "201" -> "Recurso criado";
            case "204" -> "Recurso removido";
            case "400" -> "Dados inválidos";
            case "404" -> "Recurso não encontrado";
            default -> "Resposta da operação";
        };
    }

    private Schema<?> ref(String schemaName) {
        return new Schema<>().$ref("#/components/schemas/" + schemaName);
    }

    private Schema<?> schemaFor(CrudRegistration registration) {
        Class<?> type = registration.dto() ? registration.apiClass() : registration.entityClass();
        ObjectSchema schema = new ObjectSchema();
        Map<String, Schema> properties = new LinkedHashMap<>();
        for (Field field : allFields(type).values()) {
            if (field.isSynthetic() || field.isAnnotationPresent(JsonIgnore.class)) {
                continue;
            }
            properties.put(field.getName(), new Schema<>().type(typeName(field.getType())));
        }
        schema.setProperties(properties);
        return schema;
    }

    private String typeName(Class<?> type) {
        if (type == String.class || type.isEnum()) return "string";
        if (type == boolean.class || type == Boolean.class) return "boolean";
        if (type == int.class || type == Integer.class || type == long.class || type == Long.class
                || type == short.class || type == Short.class) return "integer";
        if (Number.class.isAssignableFrom(type) || type == float.class || type == double.class) return "number";
        return "object";
    }

    private Map<String, Field> allFields(Class<?> type) {
        Map<String, Field> fields = new LinkedHashMap<>();
        for (Class<?> current = type; current != null && current != Object.class; current = current.getSuperclass()) {
            for (Field field : current.getDeclaredFields()) {
                fields.putIfAbsent(field.getName(), field);
            }
        }
        return fields;
    }
}
