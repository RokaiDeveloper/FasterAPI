# FasterCRUD

Automate REST endpoints for JPA entities with an annotation.

FasterCRUD is a minimalist Spring Boot framework that automatically generates RESTful CRUD endpoints for JPA entities through a simple annotation. Eliminate the need to write repetitive Controllers, Services, and Repositories.

---

**[Português (Brasil)](#português-brasil)**

## Installation

### Maven Central

Add the version published to Maven Central to your project's `pom.xml`:

```xml
<dependency>
    <groupId>io.github.rokaideveloper</groupId>
    <artifactId>fasterapi-spring-boot-starter</artifactId>
    <version>1.0.2</version>
</dependency>
```

Published artifact: [io.github.rokaideveloper:fasterapi-spring-boot-starter on Maven Central](https://central.sonatype.com/artifact/io.github.rokaideveloper/fasterapi-spring-boot-starter).

There is no need to manually install the project with `mvn install`, copy framework classes, or declare `@ComponentScan` for internal packages. The starter is loaded automatically by Spring Boot.

In `application.properties`, define one or more packages containing your entities and DTOs annotated with `@FasterCRUD`:

```properties
fasterapi.base-package=com.yourproject.models
```

For multiple paths, prefer `fasterapi.base-packages`:

```yaml
fasterapi:
  base-packages:
    - com.yourproject.models
    - com.yourproject.dtos
    - com.yourproject.catalog
```

You can also use a comma-separated list:

```properties
fasterapi.base-packages=com.yourproject.models,com.yourproject.dtos
```

`fasterapi.base-package` remains compatible with previous versions. Paths are combined, duplicates are ignored, and annotated entities and DTOs are scanned in all of them.

When package roles should be explicit, use:

```yaml
fasterapi:
  model-packages:
    - com.example.domain
  dto-packages:
    - com.example.api.dto
```

These semantic properties are combined with `base-package` and `base-packages`, so migration can be gradual.

## Installation Validation

When starting the application, check the logs to confirm that the framework was initialized correctly. You should see messages such as:

```
[INFO] >>> Scanning package: com.yourproject.models
[INFO] >>> Entities found: 2
[INFO]    - com.yourproject.models.Cliente
[INFO]    - com.yourproject.models.Produto
[INFO] >>> CRUD registered for entity: Cliente with path: /clientes
[INFO] >>> CRUD registered for entity: Produto with path: /produtos
```

**DEBUG logs**: To see details about individual mappings, enable DEBUG level in `application.properties`:

```properties
logging.level.com.rokaidev.fasterapi=DEBUG
```

## Usage

Annotate any JPA entity with `@FasterCRUD`:

```java
@Entity
@FasterCRUD(path = "/clientes")
public class Cliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String nome;

    private String email;
    // getters and setters
}
```

Automatically generated endpoints:

| Method | URL               | Description               |
|--------|-------------------|---------------------------|
| GET    | `/{path}`         | Lists all                |
| GET    | `/{path}/{id}`    | Finds by ID              |
| POST   | `/{path}`         | Creates a new one        |
| PUT    | `/{path}/{id}`    | Updates an existing one  |
| PATCH  | `/{path}/{id}`    | Partially updates        |
| DELETE | `/{path}/{id}`    | Removes                  |

Operations can be enabled individually. A disabled operation is not registered in Spring MVC and returns 404:

```java
@Entity
@FasterCRUD(
    path = "/produtos",
    enableGet = true,
    enablePost = true,
    enablePut = true,
    enablePatch = true,
    enableDelete = false
)
public class Produto {
}
```

The `/v3/api-docs` endpoint and Swagger UI reflect the mappings actually registered in Spring MVC. Therefore, disabled operations do not appear in the documentation and are not available as endpoints.

For each resource, OpenAPI generates:

- A tag with the name of the entity or DTO annotated with `@FasterCRUD`
- A `<Resource>Response` response schema
- A `<Resource>Request` input schema for POST, PUT, and PATCH
- OpenAPI types for strings, enums, booleans, integers, numbers, and dates
- `200`, `201`, `204`, `400`, and `404` responses as applicable to the operation

Input schemas do not include the identifier or fields marked with `@ReadOnly`. Fields marked with `@JsonIgnore` are not exposed in response schemas or in documented filters.

The list GET documents these parameters:

- `page`: page starting at `0`
- `size`: number of records per page, defaulting to `10`
- `sort`: `field,direction`, using `asc` or `desc`
- Equality filters, such as `nome=Notebook` or `nome__eq=Notebook`
- `campo__like`, `campo__gt`, `campo__lt`, `campo__gte`, and `campo__lte`

The `gt`, `lt`, `gte`, and `lte` operators apply to numeric fields, according to the service behavior.

Request examples:

```bash
# Create
curl -X POST http://localhost:8080/clientes \
  -H "Content-Type: application/json" \
  -d '{"nome":"João","email":"joao@email.com"}'

# List
curl http://localhost:8080/clientes

# Find by ID
curl http://localhost:8080/clientes/1

# Update (complete replacement)
curl -X PUT http://localhost:8080/clientes/1 \
  -H "Content-Type: application/json" \
  -d '{"nome":"João Silva","email":"joao.silva@email.com"}'

# Partial update (PATCH)
curl -X PATCH http://localhost:8080/clientes/1 \
  -H "Content-Type: application/json" \
  -d '{"nome":"João Silva"}'

# Delete
curl -X DELETE http://localhost:8080/clientes/1
```

## Features

### Basic CRUD

- **Automatic type conversion**: The framework uses Jackson ObjectMapper to automatically convert JSON values to the correct types for entity fields (String, Integer, BigDecimal, etc.)
- **Validation**: Support for Jakarta Validation annotations (`@NotNull`, `@NotBlank`, `@Positive`, etc.)
- **Error handling**: Appropriate HTTP responses:
  - 400 Bad Request for validation errors
  - 404 Not Found for entities that are not found
  - 204 No Content for successful deletions
  - 201 Created for successful creations

### Pagination

The framework supports native pagination through query parameters:

```bash
GET /produtos?page=0&size=10
```

**Parameters:**

- `page`: Page number (starts at 0, default: 0)
- `size`: Page size (default: 10)

**Response:**

```json
{
  "content": [...],
  "totalElements": 100,
  "totalPages": 10,
  "number": 0,
  "size": 10,
  "first": true,
  "last": false
}
```

### Sorting

Sort results by any field:

```bash
GET /produtos?sort=nome,asc
GET /produtos?sort=preco,desc
```

**Syntax:** `sort=field,direction`

**Directions:**

- `asc` - Ascending (default)
- `desc` - Descending

**Multiple fields:**

```bash
GET /produtos?sort=nome,asc&sort=preco,desc
```

### Dynamic Filters

Filter results using query parameters with operators:

```bash
GET /produtos?nome=Notebook
GET /produtos?preco__gt=100
GET /produtos?nome__like=Dell
```

**Supported operators:**

- No suffix: Exact equality (`eq`)
- `__like`: Partial search (LIKE)
- `__gt`: Greater Than
- `__lt`: Less Than
- `__gte`: Greater Than or Equal
- `__lte`: Less Than or Equal

**Examples:**

```bash
# Equality
GET /produtos?nome=Notebook

# LIKE (partial search)
GET /produtos?nome__like=Dell

# Greater than
GET /produtos?preco__gt=1000

# Less than
GET /produtos?preco__lt=5000

# Combining filters
GET /produtos?nome__like=Dell&preco__gt=2000

# Filters with pagination
GET /produtos?preco__gt=1000&page=0&size=10&sort=preco,asc
```

**Note:** Numeric operators (`gt`, `lt`, `gte`, `lte`) only work on fields of type `Number` (Integer, Long, BigDecimal, etc.).

### Custom Filters with @FasterFilter

For custom controllers that need filtering beyond the auto-generated CRUD endpoints, use the `@FasterFilter` annotation to enable validated dynamic filtering:

```java
@RestController
@RequestMapping("/custom-produtos")
public class CustomProdutoController {

    @GetMapping
    public Object list(@FasterFilter(
        entity = Produto.class,
        fields = {"nome", "preco"},
        strict = true
    ) FasterFilters<Produto> filters) {
        return repository.findAll(filters.getSpecification());
    }
}
```

**Parameters:**

- `entity`: The JPA entity class to validate filter fields against
- `fields`: Array of allowed field names for filtering (whitelist)
- `strict`: If `true` (default), throws HTTP 400 for invalid/unknown filters. If `false`, silently ignores invalid filters

**Behavior:**

- Filters are validated against the entity's fields and the allowed fields list
- Invalid operators or field types return HTTP 400 with a descriptive error message
- Only fields listed in `fields` are accepted when filtering
- The `FasterFilters<T>` wrapper provides both the raw filter map and the validated `Specification<T>`

**Example usage:**

```bash
# Valid filter (field is in allowed list)
GET /custom-produtos?nome=Notebook

# Invalid field (not in allowed list) - returns 400
GET /custom-produtos?descricao=Dell
# Response: Campo 'descricao' não é permitido para filtro. Campos permitidos: [nome, preco]

# Invalid operator - returns 400
GET /custom-produtos?nome__invalid=valor
# Response: Operador 'invalid' não suportado. Operadores válidos: eq, like, gt, lt, gte, lte

# Type mismatch - returns 400
GET /custom-produtos?preco__like=5000
# Response: Operador 'like' só pode ser usado em campos do tipo String
```

### Field Protection

Use Jackson annotations to control field exposure:

```java
@Entity
@FasterCRUD(path = "/usuarios")
public class Usuario {
    @Id
    private Long id;

    private String nome;

    @JsonIgnore
    private String senha;  // Not exposed in JSON responses
}
```

### Read-Only Fields

Use the `@ReadOnly` annotation to prevent fields from being modified through the API:

```java
@Entity
@FasterCRUD(path = "/produtos")
public class Produto {
    @Id
    private Long id;

    private String nome;

    @ReadOnly
    private String codigoInterno;  // Ignored in POST/PUT/PATCH
}
```

**Behavior:**

- The field can be read in responses
- Attempts to modify it through POST/PUT/PATCH are silently ignored
- Useful for automatically generated or sensitive fields

### JPA Relationships

The framework natively supports JPA relationships:

```java
@Entity
@FasterCRUD(path = "/pedidos")
public class Pedido {
    @Id
    private Long id;

    private String descricao;

    @ManyToOne
    private Cliente cliente;

    @OneToMany(mappedBy = "pedido")
    private List<ItemPedido> itens;
}
```

**Recommendations:**

- Use `@JsonIgnore` on bidirectional relationships to avoid serialization cycles
- Consider using DTOs for public APIs

### JPA Inheritance

Inheritance is managed by the client project's JPA provider. Automatic CRUD should be placed on the concrete root of the model only when the serialization contract and identifier are appropriate for the API.

```java
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@FasterCRUD(path = "/pessoas")
public abstract class Pessoa {
    @Id
    private Long id;
    private String nome;
}

@Entity
public class PessoaFisica extends Pessoa {
    private String cpf;
}

@Entity
public class PessoaJuridica extends Pessoa {
    private String cnpj;
}
```

### Optimistic Locking

Use `@Version` for concurrency control:

```java
@Entity
@FasterCRUD(path = "/produtos")
public class Produto {
    @Id
    private Long id;

    private String nome;

    @Version
    private Long version;  // Automatically incremented on each update
}
```

JPA increments the version and handles conflicts according to the project's configuration. FasterAPI does not transform concurrency exceptions into a specific HTTP contract.

### Auditing

Auditing is not included in the starter. If the application uses Spring Data Auditing, Envers, or another mechanism, add and configure those dependencies in the client project. Audit fields exposed through CRUD follow the same serialization and writing rules as the entity, unless `@ReadOnly` or `@JsonIgnore` is used.

### OpenAPI/Swagger Documentation

The framework automatically integrates with SpringDoc OpenAPI when the optional dependency is present. To enable Swagger in the consuming project, add:

```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.3.0</version>
</dependency>
```

With SpringDoc present, the framework provides automatic OpenAPI documentation.

By default, the integration is enabled only when SpringDoc is available on the classpath. It can be explicitly controlled:

```properties
# default: true
fasterapi.openapi.enabled=true
```

To keep CRUD active without registering Swagger customization:

```properties
fasterapi.openapi.enabled=false
```

If the client project does not include SpringDoc, FasterAPI continues to work normally; only `/swagger-ui.html` and `/v3/api-docs` will not be provided by the framework.

SpringDoc routes can also be customized by the client project, using SpringDoc official properties:

```properties
# OpenAPI JSON document route
springdoc.api-docs.path=/docs/openapi

# Swagger UI route
springdoc.swagger-ui.path=/docs/swagger
```

In this example, the URLs will be:

```text
/docs/openapi
/docs/swagger
```

FasterAPI does not create its own Swagger route. It registers CRUDs in the OpenAPI instance managed by SpringDoc, so the documentation automatically appears at the custom route chosen by the client. The application can also disable only the visual interface:

```properties
springdoc.swagger-ui.enabled=false
```

**Accessing the documentation:**

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

**Configuration:**

The `OpenApiConfig` class is already included and configured with basic project information. It is not necessary to create static controllers merely to make the generated CRUDs appear in Swagger.

### DTO Support

The framework supports two approaches for working with DTOs:

#### Option 1 - Automatic DTO (Default)

By default, when you use `@FasterCRUD` on an entity, the framework automatically filters fields marked with `@JsonIgnore` from API responses:

```java
@Entity
@FasterCRUD(path = "/usuarios")
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private String email;

    @JsonIgnore
    private String senha;  // Not exposed in the API

    @JsonIgnore
    private String token;  // Not exposed in the API

    // getters and setters
}
```

**Behavior:**

- POST/PUT/PATCH requests accept all fields (including sensitive ones)
- GET responses return only fields without `@JsonIgnore`
- Fields with `@JsonIgnore` are saved in the database but not exposed in the API

#### Option 2 - Manual DTO (Recommended for Public APIs)

Create custom DTOs and use `@FasterCRUD` with `@EntityMapping`:

```java
// JPA entity
@Entity
public class Produto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private String descricao;
    private Double preco;

    // Internal field not exposed
    private String codigoInterno;

    // getters and setters
}

// API DTO
@FasterCRUD(path = "/produtos", isDto = true)
@EntityMapping(entity = Produto.class)
public class ProdutoDTO {
    private Long id;
    private String nome;
    private String descricao;
    private Double preco;

    // Does not include codigoInterno - sensitive field

    // getters and setters
}
```

**Advantages of using manual DTOs:**

- Full control over which fields are exposed
- Clear separation between domain model and API
- Ability to transform data before exposing it
- Greater security for public APIs

**Behavior:**

- POST/PUT/PATCH requests receive DTO → mapped to Entity → saved
- GET responses return Entity → mapped to DTO → JSON
- Only fields with the same name are mapped automatically
- OpenAPI uses manual DTO fields to generate schemas and filters, without documenting additional fields that exist only in the entity

## Dependencies and Java Compatibility

Artifact `1.0.2` is compiled with `--release 17`. Therefore, the consuming application must run on Java 17 or higher. Spring Boot 3.2 also requires Java 17 or higher.

In practical terms:

| Application runtime | FasterAPI 1.0.2 |
|---------------------|-----------------|
| Java 17             | Supported       |
| Java 18, 19, or 20  | Supported       |
| Java 21             | Supported       |
| Java 22+            | Supported, provided it is compatible with the Spring Boot version used |

The starter is intended for Spring Boot 3.x applications with Java 17 or higher and integrates Spring MVC Servlet, Spring Data JPA, Jakarta Persistence, Jakarta Bean Validation, and Jackson.

The consumer must provide the database driver and configuration. The starter does not include a production database and does not impose H2 on the application.

SpringDoc is optional. To enable the documentation, add the dependency described in the [OpenAPI/Swagger Documentation](#openapiswagger-documentation) section.

FasterAPI does not use MapStruct or Spring Data Envers. Auditing, authentication, authorization, and business rules remain the application's responsibilities.

## Project Tests

Run the tests with:

```bash
./mvnw test
```

To validate the complete Maven artifact:

```bash
./mvnw clean test package
```

The suite covers the CRUD service, JPA entities, DTOs, pagination, sorting, filters, `@ReadOnly`, selective operations, and OpenAPI documentation.

## Advanced Examples

See the [FasterAPI demo project](https://github.com/RokaiDeveloper/fasterAPIdemo) for a complete usage example.

### Complete Entity with All Features

```java
@Entity
@FasterCRUD(path = "/produtos")
public class Produto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name is required")
    private String nome;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    private BigDecimal preco;

    private String descricao;

    @ReadOnly
    private String codigoSku;  // Internally generated field

    @JsonIgnore
    private String chaveInterna;  // Not exposed in the API

    @Version
    private Long version;  // Concurrency control

    @CreatedDate
    private LocalDateTime dataCriacao;

    @LastModifiedDate
    private LocalDateTime dataAtualizacao;

    @ManyToOne
    @JsonIgnore
    private Categoria categoria;  // Prevents serialization cycle

    // getters and setters
}
```

### Using All Features Together

```bash
# List paginated, sorted, and filtered products
GET /produtos?page=0&size=20&sort=preco,desc&nome__like=Dell&preco__gt=2000

# Expected response:
{
  "content": [
    {
      "id": 1,
      "nome": "Notebook Dell XPS",
      "preco": 8500.00,
      "descricao": "Notebook premium",
      "codigoSku": "SKU-001",
      "version": 1,
      "dataCriacao": "2026-10-09T10:00:00",
      "dataAtualizacao": "2026-10-09T10:00:00"
    }
  ],
  "totalElements": 5,
  "totalPages": 1,
  "number": 0,
  "size": 20,
  "first": true,
  "last": true
}
```

## Best Practices

### 1. Validation

Always use Jakarta validation annotations on entity fields:

```java
@NotBlank(message = "Name cannot be empty")
@Size(min = 3, max = 100, message = "Name must be between 3 and 100 characters")
private String nome;

@NotNull(message = "Price is required")
@Positive(message = "Price must be positive")
private BigDecimal preco;
```

### 2. Sensitive Fields

Use `@JsonIgnore` for fields that should not be exposed:

```java
@JsonIgnore
private String senha;

@JsonIgnore
private String token;
```

### 3. Calculated Fields

Use `@ReadOnly` for fields that are calculated or generated:

```java
@ReadOnly
private String codigoGerado;
```

### 4. Relationships

Avoid serialization cycles in bidirectional relationships:

```java
@Entity
public class Pedido {
    @OneToMany(mappedBy = "pedido")
    @JsonIgnore  // Important: prevents infinite cycle
    private List<ItemPedido> itens;
}

@Entity
public class ItemPedido {
    @ManyToOne
    private Pedido pedido;
}
```

### 5. Auditing

FasterAPI does not implement auditing. To use `@CreatedDate`, `@LastModifiedDate`, Envers, or another history mechanism, configure it in the client project and keep those fields out of automatic operations when the application requires specific rules.

## Troubleshooting

### The application starts, but no CRUD is registered

The scanner uses `fasterapi.base-package`, `fasterapi.base-packages`, `fasterapi.model-packages`, and `fasterapi.dto-packages` as roots and looks for classes annotated with `@FasterCRUD`; it does not look only for classes marked with `@Entity`.

Confirm that the configured packages contain entities and DTOs on the classpath and enable logging to follow registration. If no package is configured, FasterAPI logs a warning and does not create CRUDs:

```properties
logging.level.com.rokaidev.fasterapi=DEBUG
```

### The context fails while creating FasterAPI

The starter requires a Servlet web application with JPA available. WebFlux, plain JDBC, or an application without an `EntityManager` do not meet the current contract. The client project must also provide a datasource, a JPA provider, and entities with `@Id`; current CRUD identifiers are treated as `Long`.

### There is a mapping conflict at startup

Mappings are registered dynamically at startup. A conflict occurs when two resources use the same path and HTTP method combination, or when an application route coincides with a generated CRUD route. Use unique paths in `@FasterCRUD(path = "...")` and do not create a second controller for the same URL.

### Swagger does not appear

Documentation is created only when the client project includes `springdoc-openapi-starter-webmvc-ui` and `fasterapi.openapi.enabled` is not set to `false`. Also check that the client has not disabled:

```properties
springdoc.api-docs.enabled=false
springdoc.swagger-ui.enabled=false
```

When routes have been customized, use the paths configured in `springdoc.api-docs.path` and `springdoc.swagger-ui.path`.

### The documentation shows unexpected schemas or filters

For automatic DTOs, the documentation uses entity fields that do not have `@JsonIgnore`. For manual DTOs, it uses the fields of the type annotated with `@FasterCRUD(isDto = true)`, not all fields of the mapped entity. `@ReadOnly` is removed from input schemas and `@JsonIgnore` removes fields from the exposed schemas and documented filters.

### A filter returns no results or does not change the query

The name must match the exposed Java field of the entity or DTO. `gt`, `lt`, `gte`, and `lte` are numeric operators; `like` is a partial text search. Unknown fields are ignored by the service, so a typo may look like a query without a filter. When combining filters, all predicates are applied together.

### The list response has a different format

Without `page`, `size`, and `sort`, the list returns a JSON collection. When any pagination or sorting parameter is sent, it returns a paginated object with `content`, `totalElements`, `totalPages`, `number`, `size`, `first`, and `last`. This behavior should be reflected in the generated client or the consumed OpenAPI contract.

## Limitations

- **Stack:** current support is Spring MVC Servlet + Spring Data JPA; WebFlux, plain JDBC, MongoDB, and R2DBC are not supported adapters.
- **Identifier:** CRUD identifiers are treated as `Long`.
- **AOT/native:** the scanner and mappings are configured at runtime; native/AOT applications may require specific runtime-hints integration.
- **Domain:** the service does not replace business rules, authorization, or composite transactions.
- **Relationships:** custom DTOs may be necessary to avoid cycles, unexpected lazy loading, or large responses.
- **Filters:** dynamic filters are based on direct fields; complex queries should use specific endpoints or repositories.

## Contributing

Contributions are welcome! Feel free to:

1. Fork the project
2. Create a branch for your feature (`git checkout -b feature/new-feature`)
3. Commit your changes (`git commit -m 'Add new feature'`)
4. Push to the branch (`git push origin feature/new-feature`)
5. Open a Pull Request

## License

This project is under the FasterAPI Proprietary Free-Use License. The framework may be used as a dependency in personal and commercial applications, but FasterAPI code may not be copied, modified, redistributed, or used to create derivative versions without written authorization. See the [LICENSE](LICENSE) file for the complete terms.

## Support

For problems, questions, or suggestions:

- Open an issue on GitHub
- Consult the Spring Boot documentation
- Consult the Spring Data JPA documentation

---

# Português (Brasil)

Automatize endpoints REST para entidades JPA com uma anotação.

FasterCRUD é um framework minimalista para Spring Boot que gera automaticamente endpoints CRUD RESTful para entidades JPA através de uma simples anotação. Elimine a necessidade de escrever Controllers, Services e Repositories repetitivos.

## Instalação

### Maven Central

Adicione a versão publicada no Maven Central ao `pom.xml` do seu projeto:

```xml
<dependency>
    <groupId>io.github.rokaideveloper</groupId>
    <artifactId>fasterapi-spring-boot-starter</artifactId>
    <version>1.0.2</version>
</dependency>
```

Artefato publicado: [io.github.rokaideveloper:fasterapi-spring-boot-starter no Maven Central](https://central.sonatype.com/artifact/io.github.rokaideveloper/fasterapi-spring-boot-starter).

Não é necessário instalar o projeto manualmente com `mvn install`, copiar classes do framework ou declarar `@ComponentScan` para os pacotes internos. O starter é carregado automaticamente pelo Spring Boot.

No arquivo `application.properties`, defina um ou mais pacotes onde estão suas entidades e DTOs anotados com `@FasterCRUD`:

```properties
fasterapi.base-package=com.seuprojeto.models
```

Para múltiplos caminhos, prefira `fasterapi.base-packages`:

```yaml
fasterapi:
  base-packages:
    - com.seuprojeto.models
    - com.seuprojeto.dtos
    - com.seuprojeto.catalogo
```

Também é possível usar uma lista separada por vírgulas:

```properties
fasterapi.base-packages=com.seuprojeto.models,com.seuprojeto.dtos
```

`fasterapi.base-package` continua compatível com versões anteriores. Os caminhos são combinados, duplicatas são ignoradas e entidades e DTOs anotados são procurados em todos eles.

Quando os papéis dos pacotes precisam ficar explícitos, use:

```yaml
fasterapi:
  model-packages:
    - com.seuprojeto.domain
  dto-packages:
    - com.seuprojeto.api.dto
```

As propriedades semânticas são combinadas com `base-package` e `base-packages`, portanto você pode migrar gradualmente.

## Validação da instalação

Ao iniciar a aplicação, verifique os logs para confirmar que o framework foi inicializado corretamente. Você deve ver mensagens como:

```
[INFO] >>> Escaneando pacote: com.seuprojeto.models
[INFO] >>> Entidades encontradas: 2
[INFO]    - com.seuprojeto.models.Cliente
[INFO]    - com.seuprojeto.models.Produto
[INFO] >>> CRUD registrado para entidade: Cliente com path: /clientes
[INFO] >>> CRUD registrado para entidade: Produto com path: /produtos
```

**Logs em DEBUG**: Para ver detalhes dos mapeamentos individuais, habilite o nível DEBUG no `application.properties`:

```properties
logging.level.com.rokaidev.fasterapi=DEBUG
```

## Uso

Anote qualquer entidade JPA com `@FasterCRUD`:

```java
@Entity
@FasterCRUD(path = "/clientes")
public class Cliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String nome;

    private String email;
    // getters e setters
}
```

Endpoints gerados automaticamente:

| Método | URL               | Descrição                |
|--------|-------------------|--------------------------|
| GET    | `/{path}`         | Lista todos              |
| GET    | `/{path}/{id}`    | Busca por ID             |
| POST   | `/{path}`         | Cria um novo             |
| PUT    | `/{path}/{id}`    | Atualiza existente       |
| PATCH  | `/{path}/{id}`    | Atualização parcial      |
| DELETE | `/{path}/{id}`    | Remove                   |

As operações podem ser habilitadas individualmente. Uma operação desabilitada não é registrada no Spring MVC e retorna 404:

```java
@Entity
@FasterCRUD(
    path = "/produtos",
    enableGet = true,
    enablePost = true,
    enablePut = true,
    enablePatch = true,
    enableDelete = false
)
public class Produto {
}
```

O endpoint `/v3/api-docs` e o Swagger UI refletem os mappings efetivamente registrados no Spring MVC. Portanto, operações desabilitadas não aparecem na documentação nem ficam disponíveis como endpoints.

Para cada recurso, o OpenAPI gera:

- Uma tag com o nome da entidade ou DTO anotado com `@FasterCRUD`
- Schema de resposta `<Recurso>Response`
- Schema de entrada `<Recurso>Request` para POST, PUT e PATCH
- Tipos OpenAPI para strings, enums, booleanos, inteiros, números e datas
- Respostas `200`, `201`, `204`, `400` e `404` conforme a operação

Schemas de entrada não incluem o identificador nem campos marcados com `@ReadOnly`. Campos marcados com `@JsonIgnore` não são expostos nos schemas de resposta nem nos filtros documentados.

O GET de listagem documenta os parâmetros:

- `page`: página iniciando em `0`
- `size`: quantidade de registros por página, com padrão `10`
- `sort`: `campo,direcao`, usando `asc` ou `desc`
- Filtros por igualdade, como `nome=Notebook` ou `nome__eq=Notebook`
- `campo__like`, `campo__gt`, `campo__lt`, `campo__gte` e `campo__lte`

Os operadores `gt`, `lt`, `gte` e `lte` são aplicáveis a campos numéricos, conforme o comportamento do serviço.

Exemplos de requisições:

```bash
# Criar
curl -X POST http://localhost:8080/clientes \
  -H "Content-Type: application/json" \
  -d '{"nome":"João","email":"joao@email.com"}'

# Listar
curl http://localhost:8080/clientes

# Buscar por ID
curl http://localhost:8080/clientes/1

# Atualizar (substituição completa)
curl -X PUT http://localhost:8080/clientes/1 \
  -H "Content-Type: application/json" \
  -d '{"nome":"João Silva","email":"joao.silva@email.com"}'

# Atualização parcial (PATCH)
curl -X PATCH http://localhost:8080/clientes/1 \
  -H "Content-Type: application/json" \
  -d '{"nome":"João Silva"}'

# Deletar
curl -X DELETE http://localhost:8080/clientes/1
```

## Funcionalidades

### CRUD Básico
- **Conversão automática de tipos**: O framework usa Jackson ObjectMapper para converter automaticamente valores JSON para os tipos corretos dos campos da entidade (String, Integer, BigDecimal, etc.)
- **Validação**: Suporte a anotações de validação Jakarta Validation (@NotNull, @NotBlank, @Positive, etc.)
- **Tratamento de erros**: Respostas HTTP apropriadas:
  - 400 Bad Request para erros de validação
  - 404 Not Found para entidades não encontradas
  - 204 No Content para deleções bem-sucedidas
  - 201 Created para criações bem-sucedidas

### Paginação
O framework suporta paginação nativa através de query parameters:

```bash
GET /produtos?page=0&size=10
```

**Parâmetros:**
- `page`: Número da página (começa em 0, padrão: 0)
- `size`: Tamanho da página (padrão: 10)

**Resposta:**
```json
{
  "content": [...],
  "totalElements": 100,
  "totalPages": 10,
  "number": 0,
  "size": 10,
  "first": true,
  "last": false
}
```

### Ordenação
Ordene os resultados por qualquer campo:

```bash
GET /produtos?sort=nome,asc
GET /produtos?sort=preco,desc
```

**Sintaxe:** `sort=campo,direcao`

**Direções:**
- `asc` - Ascendente (padrão)
- `desc` - Descendente

**Múltiplos campos:**
```bash
GET /produtos?sort=nome,asc&sort=preco,desc
```

### Filtros Dinâmicos
Filtre resultados usando query parameters com operadores:

```bash
GET /produtos?nome=Notebook
GET /produtos?preco__gt=100
GET /produtos?nome__like=Dell
```

**Operadores suportados:**
- Sem sufixo: Igualdade exata (`eq`)
- `__like`: Busca parcial (LIKE)
- `__gt`: Maior que (Greater Than)
- `__lt`: Menor que (Less Than)
- `__gte`: Maior ou igual (Greater Than or Equal)
- `__lte`: Menor ou igual (Less Than or Equal)

**Exemplos:**
```bash
# Igualdade
GET /produtos?nome=Notebook

# LIKE (busca parcial)
GET /produtos?nome__like=Dell

# Maior que
GET /produtos?preco__gt=1000

# Menor que
GET /produtos?preco__lt=5000

# Combinando filtros
GET /produtos?nome__like=Dell&preco__gt=2000

# Filtros com paginação
GET /produtos?preco__gt=1000&page=0&size=10&sort=preco,asc
```

**Nota:** Operadores numéricos (`gt`, `lt`, `gte`, `lte`) só funcionam em campos do tipo `Number` (Integer, Long, BigDecimal, etc).

### Proteção de Campos
Use anotações do Jackson para controlar a exposição de campos:

```java
@Entity
@FasterCRUD(path = "/usuarios")
public class Usuario {
    @Id
    private Long id;

    private String nome;

    @JsonIgnore
    private String senha;  // Não será exposto nas respostas JSON
}
```

### Campos Read-Only
Use a annotation `@ReadOnly` para impedir modificação de campos via API:

```java
@Entity
@FasterCRUD(path = "/produtos")
public class Produto {
    @Id
    private Long id;

    private String nome;

    @ReadOnly
    private String codigoInterno;  // Ignorado em POST/PUT/PATCH
}
```

**Comportamento:**
- O campo pode ser lido nas respostas
- Tentativas de modificar via POST/PUT/PATCH são ignoradas silenciosamente
- Útil para campos gerados automaticamente ou sensíveis

### Relacionamentos JPA
O framework suporta nativamente relacionamentos JPA:

```java
@Entity
@FasterCRUD(path = "/pedidos")
public class Pedido {
    @Id
    private Long id;

    private String descricao;

    @ManyToOne
    private Cliente cliente;

    @OneToMany(mappedBy = "pedido")
    private List<ItemPedido> itens;
}
```

**Recomendações:**
- Use `@JsonIgnore` em relacionamentos bidirecionais para evitar ciclos de serialização
- Considere usar DTOs para APIs públicas

### Herança JPA

A herança é gerenciada pelo provedor JPA do projeto cliente. O CRUD automático deve ser colocado na raiz concreta do modelo somente quando o contrato de serialização e o identificador forem adequados à API.

```java
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@FasterCRUD(path = "/pessoas")
public abstract class Pessoa {
    @Id
    private Long id;
    private String nome;
}

@Entity
public class PessoaFisica extends Pessoa {
    private String cpf;
}

@Entity
public class PessoaJuridica extends Pessoa {
    private String cnpj;
}
```

### Concorrência Otimista
Use `@Version` para controle de concorrência:

```java
@Entity
@FasterCRUD(path = "/produtos")
public class Produto {
    @Id
    private Long id;

    private String nome;

    @Version
    private Long version;  // Incrementado automaticamente em cada atualização
}
```

O JPA incrementa o version e trata conflitos conforme a configuração do projeto. O FasterAPI não transforma exceções de concorrência em um contrato HTTP específico.

### Auditoria

Auditoria não é incluída no starter. Caso a aplicação use Spring Data Auditing, Envers ou outro mecanismo, adicione e configure essas dependências no projeto cliente. Campos de auditoria expostos no CRUD seguem as mesmas regras de serialização e escrita da entidade, salvo uso de `@ReadOnly` ou `@JsonIgnore`.

### Documentação OpenAPI/Swagger
O framework integra automaticamente com SpringDoc OpenAPI quando a dependência opcional está presente. Para habilitar Swagger no projeto consumidor, adicione:

```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.3.0</version>
</dependency>
```

Com SpringDoc presente, o framework fornece documentação automática via OpenAPI.

Por padrão, a integração é ativada somente quando SpringDoc está disponível no classpath. Ela pode ser controlada explicitamente:

```properties
# padrão: true
fasterapi.openapi.enabled=true
```

Para manter o CRUD ativo sem registrar a personalização Swagger:

```properties
fasterapi.openapi.enabled=false
```

Se o projeto cliente não incluir SpringDoc, o FasterAPI continua funcionando normalmente; apenas `/swagger-ui.html` e `/v3/api-docs` não serão fornecidos pelo framework.

As rotas do SpringDoc também podem ser personalizadas pelo projeto cliente, usando as propriedades oficiais do SpringDoc:

```properties
# Rota do documento OpenAPI JSON
springdoc.api-docs.path=/documentacao/openapi

# Rota da interface Swagger UI
springdoc.swagger-ui.path=/documentacao/swagger
```

Nesse exemplo, as URLs serão:

```text
/documentacao/openapi
/documentacao/swagger
```

O FasterAPI não cria uma rota própria para o Swagger. Ele registra os CRUDs na instância OpenAPI gerenciada pelo SpringDoc, portanto a documentação aparece automaticamente na rota personalizada escolhida pelo cliente. A aplicação também pode desabilitar apenas a interface visual:

```properties
springdoc.swagger-ui.enabled=false
```

**Acessar a documentação:**
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

**Configuração:**
A classe `OpenApiConfig` já está incluída e configurada com informações básicas do projeto. Não é necessário criar controllers estáticos apenas para que os CRUDs gerados apareçam no Swagger.

### Suporte a DTOs
O framework suporta duas abordagens para trabalhar com DTOs:

#### Opção 1 - DTO Automático (Padrão)
Por padrão, quando você usa `@FasterCRUD` em uma entidade, o framework automaticamente filtra campos marcados com `@JsonIgnore` nas respostas da API:

```java
@Entity
@FasterCRUD(path = "/usuarios")
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private String email;

    @JsonIgnore
    private String senha;  // Não exposto na API

    @JsonIgnore
    private String token;  // Não exposto na API

    // getters e setters
}
```

**Comportamento:**
- Requisições POST/PUT/PATCH aceitam todos os campos (incluindo sensíveis)
- Respostas GET retornam apenas campos sem `@JsonIgnore`
- Campos com `@JsonIgnore` são salvos no banco mas não expostos na API

#### Opção 2 - DTO Manual (Recomendado para APIs Públicas)
Crie DTOs personalizados e use `@FasterCRUD` com `@EntityMapping`:

```java
// Entidade JPA
@Entity
public class Produto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private String descricao;
    private Double preco;

    // Campo interno não exposto
    private String codigoInterno;

    // getters e setters
}

// DTO para API
@FasterCRUD(path = "/produtos", isDto = true)
@EntityMapping(entity = Produto.class)
public class ProdutoDTO {
    private Long id;
    private String nome;
    private String descricao;
    private Double preco;

    // Não inclui codigoInterno - campo sensível

    // getters e setters
}
```

**Vantagens de usar DTOs manuais:**
- Controle total sobre quais campos são expostos
- Separação clara entre modelo de domínio e API
- Possibilidade de transformar dados antes de expor
- Maior segurança para APIs públicas

**Comportamento:**
- Requisições POST/PUT/PATCH recebem DTO → mapeado para Entidade → salvo
- Respostas GET retornam Entidade → mapeado para DTO → JSON
- Apenas campos com mesmo nome são mapeados automaticamente
- O OpenAPI usa os campos do DTO manual para gerar os schemas e filtros, sem documentar campos adicionais existentes somente na entidade

## Dependências e compatibilidade Java

O artefato `1.0.2` é compilado com `--release 17`. Portanto, a aplicação consumidora precisa executar em Java 17 ou superior. O Spring Boot 3.2 também exige Java 17 ou superior.

Em termos práticos:

| Runtime da aplicação | FasterAPI 1.0.2 |
|----------------------|-----------------|
| Java 17               | Suportado       |
| Java 18, 19 ou 20     | Suportado       |
| Java 21               | Suportado       |
| Java 22+              | Suportado, desde que compatível com o Spring Boot usado |

O starter é destinado a aplicações Spring Boot 3.x com Java 17 ou superior e integra Spring MVC Servlet, Spring Data JPA, Jakarta Persistence, Jakarta Bean Validation e Jackson.

O consumidor precisa fornecer o driver e a configuração do banco de dados. O starter não inclui banco de dados de produção e não impõe H2 à aplicação.

SpringDoc é opcional. Para habilitar a documentação, adicione a dependência descrita na seção [Documentação OpenAPI/Swagger](#documentação-openapiswagger).

O FasterAPI não usa MapStruct nem Spring Data Envers. Auditoria, autenticação, autorização e regras de negócio continuam sendo responsabilidades da aplicação.

## Testes do projeto

Execute os testes com:

```bash
./mvnw test
```

Para validar o artefato Maven completo:

```bash
./mvnw clean test package
```

A suíte cobre o serviço CRUD, entidades JPA, DTOs, paginação, ordenação, filtros, `@ReadOnly`, operações seletivas e documentação OpenAPI.

## Exemplos Avançados

Veja o [projeto de demonstração do FasterAPI](https://github.com/RokaiDeveloper/fasterAPIdemo) para um exemplo completo de uso.

### Entidade Completa com Todas as Funcionalidades

```java
@Entity
@FasterCRUD(path = "/produtos")
public class Produto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Nome é obrigatório")
    private String nome;

    @NotNull(message = "Preço é obrigatório")
    @Positive(message = "Preço deve ser positivo")
    private BigDecimal preco;

    private String descricao;

    @ReadOnly
    private String codigoSku;  // Campo gerado internamente

    @JsonIgnore
    private String chaveInterna;  // Não exposto na API

    @Version
    private Long version;  // Controle de concorrência

    @CreatedDate
    private LocalDateTime dataCriacao;

    @LastModifiedDate
    private LocalDateTime dataAtualizacao;

    @ManyToOne
    @JsonIgnore
    private Categoria categoria;  // Evita ciclo de serialização

    // getters e setters
}
```

### Usando Todas as Funcionalidades Juntas

```bash
# Listar produtos paginados, ordenados e filtrados
GET /produtos?page=0&size=20&sort=preco,desc&nome__like=Dell&preco__gt=2000

# Resposta esperada:
{
  "content": [
    {
      "id": 1,
      "nome": "Notebook Dell XPS",
      "preco": 8500.00,
      "descricao": "Notebook premium",
      "codigoSku": "SKU-001",
      "version": 1,
      "dataCriacao": "2026-10-09T10:00:00",
      "dataAtualizacao": "2026-10-09T10:00:00"
    }
  ],
  "totalElements": 5,
  "totalPages": 1,
  "number": 0,
  "size": 20,
  "first": true,
  "last": true
}
```

## Boas Práticas

### 1. Validação
Sempre use anotações de validação Jakarta nos campos da entidade:

```java
@NotBlank(message = "Nome não pode estar vazio")
@Size(min = 3, max = 100, message = "Nome deve ter entre 3 e 100 caracteres")
private String nome;

@NotNull(message = "Preço é obrigatório")
@Positive(message = "Preço deve ser positivo")
private BigDecimal preco;
```

### 2. Campos Sensíveis
Use `@JsonIgnore` para campos que não devem ser expostos:

```java
@JsonIgnore
private String senha;

@JsonIgnore
private String token;
```

### 3. Campos Calculados
Use `@ReadOnly` para campos que são calculados ou gerados:

```java
@ReadOnly
private String codigoGerado;
```

### 4. Relacionamentos
Evite ciclos de serialização em relacionamentos bidirecionais:

```java
@Entity
public class Pedido {
    @OneToMany(mappedBy = "pedido")
    @JsonIgnore  // Importante: evita ciclo infinito
    private List<ItemPedido> itens;
}

@Entity
public class ItemPedido {
    @ManyToOne
    private Pedido pedido;
}
```

### 5. Auditoria

O FasterAPI não implementa auditoria. Para usar `@CreatedDate`, `@LastModifiedDate`, Envers ou outro mecanismo de histórico, configure-o no projeto cliente e mantenha esses campos fora das operações automáticas quando a aplicação exigir regras específicas.

## Troubleshooting

### A aplicação inicia, mas nenhum CRUD é registrado

O scanner usa `fasterapi.base-package`, `fasterapi.base-packages`, `fasterapi.model-packages` e `fasterapi.dto-packages` como raízes e procura classes anotadas com `@FasterCRUD`; ele não procura apenas classes marcadas com `@Entity`. Confirme que os pacotes configurados contêm entidades e DTOs no classpath e habilite o log para acompanhar o registro. Se nenhum pacote for configurado, o FasterAPI registra um aviso e não cria CRUDs.

```properties
logging.level.com.rokaidev.fasterapi=DEBUG
```

### O contexto falha ao criar o FasterAPI

O starter é condicionado a uma aplicação web Servlet com JPA disponível. WebFlux, JDBC puro ou uma aplicação sem `EntityManager` não atendem ao contrato atual. O projeto cliente também precisa fornecer datasource, provedor JPA e entidades com `@Id`; os identificadores CRUD atuais são tratados como `Long`.

### Existe conflito de mapping ao iniciar

Os mappings são registrados dinamicamente no startup. O conflito ocorre quando dois recursos usam a mesma combinação de caminho e método HTTP, ou quando uma rota da aplicação coincide com uma rota CRUD gerada. Use caminhos únicos em `@FasterCRUD(path = "...")` e não crie um segundo controller para a mesma URL.

### O Swagger não aparece

A documentação só é criada quando o projeto cliente inclui `springdoc-openapi-starter-webmvc-ui` e `fasterapi.openapi.enabled` não está definido como `false`. Verifique também se o cliente não desativou:

```properties
springdoc.api-docs.enabled=false
springdoc.swagger-ui.enabled=false
```

Quando as rotas foram personalizadas, use os caminhos configurados em `springdoc.api-docs.path` e `springdoc.swagger-ui.path`.

### A documentação mostra schema ou filtros inesperados

Para DTO automático, a documentação usa os campos da entidade que não possuem `@JsonIgnore`. Para DTO manual, usa os campos do tipo anotado com `@FasterCRUD(isDto = true)`, não todos os campos da entidade mapeada. `@ReadOnly` é removido dos schemas de entrada e `@JsonIgnore` remove campos da exposição e dos filtros documentados.

### Um filtro retorna resultado vazio ou não altera a consulta

O nome precisa corresponder ao campo Java da entidade ou DTO exposto. `gt`, `lt`, `gte` e `lte` são operadores numéricos; `like` é uma busca textual parcial. Campos desconhecidos são ignorados pelo serviço, portanto um erro de digitação pode parecer uma consulta sem filtro. Ao combinar filtros, todos os predicados são aplicados conjuntamente.

### A resposta de listagem tem formato diferente

Sem `page`, `size` e `sort`, a listagem retorna uma coleção JSON. Quando qualquer parâmetro de paginação ou ordenação é enviado, retorna um objeto paginado com `content`, `totalElements`, `totalPages`, `number`, `size`, `first` e `last`. Esse comportamento deve ser refletido no cliente gerado ou no contrato OpenAPI consumido.

## Limitações

- **Stack:** o suporte atual é Spring MVC Servlet + Spring Data JPA; WebFlux, JDBC puro, MongoDB e R2DBC não são adaptadores suportados.
- **Identificador:** os identificadores CRUD são tratados como `Long`.
- **AOT/native:** o scanner e os mappings são configurados em runtime; aplicações nativas/AOT podem exigir uma integração específica de runtime hints.
- **Domínio:** o serviço não substitui regras de negócio, autorização ou transações compostas.
- **Relacionamentos:** DTOs próprios podem ser necessários para evitar ciclos, lazy loading inesperado ou respostas extensas.
- **Filtros:** filtros dinâmicos são baseados em campos diretos; consultas complexas devem usar endpoints ou repositórios específicos.

## Contribuindo

Contribuições são bem-vindas! Sinta-se à vontade para:

1. Fork o projeto
2. Criar uma branch para sua feature (`git checkout -b feature/nova-funcionalidade`)
3. Commit suas mudanças (`git commit -m 'Adiciona nova funcionalidade'`)
4. Push para a branch (`git push origin feature/nova-funcionalidade`)
5. Abrir um Pull Request

## Licença

Este projeto está sob a FasterAPI Proprietary Free-Use License. O framework pode ser usado como dependência em aplicações pessoais e comerciais, mas o código do FasterAPI não pode ser copiado, modificado, redistribuído ou usado para criar versões derivadas sem autorização escrita. Veja o arquivo LICENSE para os termos completos.

## Suporte

Para problemas, dúvidas ou sugestões:

- Abra uma issue no GitHub
- Consulte a documentação do Spring Boot
- Consulte a documentação do Spring Data JPA
