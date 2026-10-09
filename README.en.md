# FasterCRUD

**Português (Brasil):** [README.md](README.md)

Automate REST endpoints for JPA entities with an annotation.

FasterCRUD is a minimalist Spring Boot framework that automatically generates
RESTful CRUD endpoints for JPA entities through a simple annotation. Eliminate
the need to write repetitive Controllers, Services, and Repositories.

## Installation

### Maven Central

Add the version published to Maven Central to your project's `pom.xml`:

```xml
<dependency>
    <groupId>io.github.rokaideveloper</groupId>
    <artifactId>fasterapi-spring-boot-starter</artifactId>
    <version>1.0.1</version>
</dependency>
```

Published artifact: [io.github.rokaideveloper:fasterapi-spring-boot-starter on
Maven Central](https://central.sonatype.com/artifact/io.github.rokaideveloper/fasterapi-spring-boot-starter).

There is no need to manually install the project with `mvn install`, copy
framework classes, or declare `@ComponentScan` for internal packages. The
starter is loaded automatically by Spring Boot.

In `application.properties`, define the package where your entities are
located:

```properties
fasterapi.base-package=com.seuprojeto.models
```

## Installation validation

When starting the application, check the logs to confirm that the framework was
initialized correctly. You should see messages such as:

```
[INFO] >>> Scanning package: com.seuprojeto.models
[INFO] >>> Entities found: 2
[INFO]    - com.seuprojeto.models.Cliente
[INFO]    - com.seuprojeto.models.Produto
[INFO] >>> CRUD registered for entity: Cliente with path: /clientes
[INFO] >>> CRUD registered for entity: Produto with path: /produtos
```

**DEBUG logs**: To see details about individual mappings, enable DEBUG level
in `application.properties`:

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
    // getters e setters
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

Operations can be enabled individually. A disabled operation is not registered
in Spring MVC and returns 404:

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

The `/v3/api-docs` endpoint and Swagger UI reflect the mappings actually
registered in Spring MVC. Therefore, disabled operations do not appear in the
documentation and are not available as endpoints.

For each resource, OpenAPI generates:

- A tag with the name of the entity or DTO annotated with `@FasterCRUD`
- A `<Resource>Response` response schema
- A `<Resource>Request` input schema for POST, PUT, and PATCH
- OpenAPI types for strings, enums, booleans, integers, numbers, and dates
- `200`, `201`, `204`, `400`, and `404` responses as applicable to the
  operation

Input schemas do not include the identifier or fields marked with `@ReadOnly`.
Fields marked with `@JsonIgnore` are not exposed in response schemas or in
documented filters.

The list GET documents these parameters:

- `page`: page starting at `0`
- `size`: number of records per page, defaulting to `10`
- `sort`: `field,direction`, using `asc` or `desc`
- Equality filters, such as `nome=Notebook` or `nome__eq=Notebook`
- `campo__like`, `campo__gt`, `campo__lt`, `campo__gte`, and `campo__lte`

The `gt`, `lt`, `gte`, and `lte` operators apply to numeric fields, according
to the service behavior.

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

- **Automatic type conversion**: The framework uses Jackson ObjectMapper to
  automatically convert JSON values to the correct types for entity fields
  (String, Integer, BigDecimal, etc.)
- **Validation**: Support for Jakarta Validation annotations (`@NotNull`,
  `@NotBlank`, `@Positive`, etc.)
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

**Note:** Numeric operators (`gt`, `lt`, `gte`, `lte`) only work on fields of
type `Number` (Integer, Long, BigDecimal, etc.).

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

Use the `@ReadOnly` annotation to prevent fields from being modified through
the API:

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

- Use `@JsonIgnore` on bidirectional relationships to avoid serialization
  cycles
- Consider using DTOs for public APIs

### JPA Inheritance

Inheritance is managed by the client project's JPA provider. Automatic CRUD
should be placed on the concrete root of the model only when the serialization
contract and identifier are appropriate for the API.

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

JPA increments the version and handles conflicts according to the project's
configuration. FasterAPI does not transform concurrency exceptions into a
specific HTTP contract.

### Auditing

Auditing is not included in the starter. If the application uses Spring Data
Auditing, Envers, or another mechanism, add and configure those dependencies
in the client project. Audit fields exposed through CRUD follow the same
serialization and writing rules as the entity, unless `@ReadOnly` or
`@JsonIgnore` is used.

### OpenAPI/Swagger Documentation

The framework automatically integrates with SpringDoc OpenAPI when the
optional dependency is present. To enable Swagger in the consuming project,
add:

```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.3.0</version>
</dependency>
```

With SpringDoc present, the framework provides automatic OpenAPI
documentation.

By default, the integration is enabled only when SpringDoc is available on the
classpath. It can be explicitly controlled:

```properties
# default: true
fasterapi.openapi.enabled=true
```

To keep CRUD active without registering Swagger customization:

```properties
fasterapi.openapi.enabled=false
```

If the client project does not include SpringDoc, FasterAPI continues to work
normally; only `/swagger-ui.html` and `/v3/api-docs` will not be provided by
the framework.

SpringDoc routes can also be customized by the client project, using SpringDoc
official properties:

```properties
# OpenAPI JSON document route
springdoc.api-docs.path=/documentacao/openapi

# Swagger UI route
springdoc.swagger-ui.path=/documentacao/swagger
```

In this example, the URLs will be:

```text
/documentacao/openapi
/documentacao/swagger
```

FasterAPI does not create its own Swagger route. It registers CRUDs in the
OpenAPI instance managed by SpringDoc, so the documentation automatically
appears at the custom route chosen by the client. The application can also
disable only the visual interface:

```properties
springdoc.swagger-ui.enabled=false
```

**Accessing the documentation:**

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

**Configuration:**

The `OpenApiConfig` class is already included and configured with basic project
information. It is not necessary to create static controllers merely to make
the generated CRUDs appear in Swagger.

### DTO Support

The framework supports two approaches for working with DTOs:

#### Option 1 - Automatic DTO (Default)

By default, when you use `@FasterCRUD` on an entity, the framework
automatically filters fields marked with `@JsonIgnore` from API responses:

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
- Fields with `@JsonIgnore` are saved in the database but not exposed in the
  API

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
- OpenAPI uses manual DTO fields to generate schemas and filters, without
  documenting additional fields that exist only in the entity

## Dependencies and Java compatibility

Artifact `1.0.1` is compiled with `--release 17`. Therefore, the consuming
application must run on Java 17 or higher. Spring Boot 3.2 also requires Java
17 or higher.

In practical terms:

| Application runtime | FasterAPI 1.0.1 |
|---------------------|-----------------|
| Java 17             | Supported       |
| Java 18, 19, or 20  | Supported       |
| Java 21             | Supported       |
| Java 22+            | Supported, provided it is compatible with the Spring Boot version used |

The starter is intended for Spring Boot 3.x applications with Java 17 or
higher and integrates Spring MVC Servlet, Spring Data JPA, Jakarta Persistence,
Jakarta Bean Validation, and Jackson.

The consumer must provide the database driver and configuration. The starter
does not include a production database and does not impose H2 on the
application.

SpringDoc is optional. To enable the documentation, add the dependency
described in the [OpenAPI/Swagger Documentation](#openapiswagger-documentation)
section.

FasterAPI does not use MapStruct or Spring Data Envers. Auditing,
authentication, authorization, and business rules remain the application's
responsibilities.

## Project tests

Run the tests with:

```bash
./mvnw test
```

To validate the complete Maven artifact:

```bash
./mvnw clean test package
```

The suite covers the CRUD service, JPA entities, DTOs, pagination, sorting,
filters, `@ReadOnly`, selective operations, and OpenAPI documentation.

## Advanced Examples

### Complete Entity with All Features

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
@NotBlank(message = "Nome não pode estar vazio")
@Size(min = 3, max = 100, message = "Nome deve ter entre 3 e 100 caracteres")
private String nome;

@NotNull(message = "Preço é obrigatório")
@Positive(message = "Preço deve ser positivo")
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

FasterAPI does not implement auditing. To use `@CreatedDate`,
`@LastModifiedDate`, Envers, or another history mechanism, configure it in the
client project and keep those fields out of automatic operations when the
application requires specific rules.

## Troubleshooting

### The application starts, but no CRUD is registered

The scanner uses `fasterapi.base-package` as its root and looks for classes
annotated with `@FasterCRUD`; it does not look only for classes marked with
`@Entity`.

Confirm that the configured package contains the entities on the classpath and
enable logging to follow registration:

```properties
logging.level.com.rokaidev.fasterapi=DEBUG
```

### The context fails while creating FasterAPI

The starter requires a Servlet web application with JPA available. WebFlux,
plain JDBC, or an application without an `EntityManager` do not meet the
current contract. The client project must also provide a datasource, a JPA
provider, and entities with `@Id`; current CRUD identifiers are treated as
`Long`.

### There is a mapping conflict at startup

Mappings are registered dynamically at startup. A conflict occurs when two
resources use the same path and HTTP method combination, or when an
application route coincides with a generated CRUD route. Use unique paths in
`@FasterCRUD(path = "...")` and do not create a second controller for the same
URL.

### Swagger does not appear

Documentation is created only when the client project includes
`springdoc-openapi-starter-webmvc-ui` and `fasterapi.openapi.enabled` is not
set to `false`. Also check that the client has not disabled:

```properties
springdoc.api-docs.enabled=false
springdoc.swagger-ui.enabled=false
```

When routes have been customized, use the paths configured in
`springdoc.api-docs.path` and `springdoc.swagger-ui.path`.

### The documentation shows unexpected schemas or filters

For automatic DTOs, the documentation uses entity fields that do not have
`@JsonIgnore`. For manual DTOs, it uses the fields of the type annotated with
`@FasterCRUD(isDto = true)`, not all fields of the mapped entity. `@ReadOnly`
is removed from input schemas and `@JsonIgnore` removes fields from the exposed
schemas and documented filters.

### A filter returns no results or does not change the query

The name must match the exposed Java field of the entity or DTO. `gt`, `lt`,
`gte`, and `lte` are numeric operators; `like` is a partial text search.
Unknown fields are ignored by the service, so a typo may look like a query
without a filter. When combining filters, all predicates are applied together.

### The list response has a different format

Without `page`, `size`, and `sort`, the list returns a JSON collection. When
any pagination or sorting parameter is sent, it returns a paginated object with
`content`, `totalElements`, `totalPages`, `number`, `size`, `first`, and `last`.
This behavior should be reflected in the generated client or the consumed
OpenAPI contract.

## Limitations

- **Stack:** current support is Spring MVC Servlet + Spring Data JPA; WebFlux,
  plain JDBC, MongoDB, and R2DBC are not supported adapters.
- **Identifier:** CRUD identifiers are treated as `Long`.
- **AOT/native:** the scanner and mappings are configured at runtime; native/AOT
  applications may require specific runtime-hints integration.
- **Domain:** the service does not replace business rules, authorization, or
  composite transactions.
- **Relationships:** custom DTOs may be necessary to avoid cycles, unexpected
  lazy loading, or large responses.
- **Filters:** dynamic filters are based on direct fields; complex queries
  should use specific endpoints or repositories.

## Contributing

Contributions are welcome! Feel free to:

1. Fork the project
2. Create a branch for your feature
   (`git checkout -b feature/new-feature`)
3. Commit your changes (`git commit -m 'Add new feature'`)
4. Push to the branch (`git push origin feature/new-feature`)
5. Open a Pull Request

## License

This project is under the FasterAPI Proprietary Free-Use License. The
framework may be used as a dependency in personal and commercial applications,
but FasterAPI code may not be copied, modified, redistributed, or used to
create derivative versions without written authorization. See the
[LICENSE](LICENSE) file for the complete terms.

## Support

For problems, questions, or suggestions:

- Open an issue on GitHub
- Consult the Spring Boot documentation
- Consult the Spring Data JPA documentation
