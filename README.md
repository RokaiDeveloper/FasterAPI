# FasterCRUD

Automatize endpoints REST para entidades JPA com uma anotação.

FasterCRUD é um framework minimalista para Spring Boot que gera automaticamente endpoints CRUD RESTful para entidades JPA através de uma simples anotação. Elimine a necessidade de escrever Controllers, Services e Repositories repetitivos.

## Instalação

### Opção 1: Como biblioteca Maven (Recomendado)

1. Compile o projeto e instale no seu repositório local Maven:

```bash
mvn clean install
```

2. Adicione a dependência no `pom.xml` do seu projeto:

```xml
<dependency>
    <groupId>com.rokaidev</groupId>
    <artifactId>fasterAPI</artifactId>
    <version>1.0</version>
</dependency>
```

3. Na sua classe principal (`@SpringBootApplication`), adicione o pacote `fasterapi` ao `@ComponentScan`:

```java
@SpringBootApplication
@ComponentScan(basePackages = {
    "com.seuprojeto",
    "annotation",
    "core"
})
public class SuaApplication {
    public static void main(String[] args) {
        SpringApplication.run(SuaApplication.class, args);
    }
}
```

4. No arquivo `application.properties`, defina o pacote onde estão suas entidades:

```properties
fasterapi.base-package=com.seuprojeto.models
```

### Opção 2: Copiando arquivos (Alternativa)

1. Crie a estrutura de pacotes e copie os arquivos do framework:

```
src/main/java/
├── annotation/
│   └── FasterCRUD.java
└── core/
    ├── FasterCrudInitializer.java
    ├── GenericCrudService.java
    ├── GenericCrudController.java
    ├── EntityNotFoundException.java
    └── ValidationException.java
```

2. Siga os passos 3 e 4 da Opção 1 acima.

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
logging.level.core=DEBUG
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
Suporte a herança JPA com estratégias de mapeamento:

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

**Comportamento:**
- O JPA incrementa o version automaticamente
- Conflitos de atualização resultam em `OptimisticLockException`

### Auditoria
Use Spring Data Envers para histórico de alterações:

```java
@Entity
@FasterCRUD(path = "/produtos")
@Audited
public class Produto {
    @Id
    private Long id;
    
    private String nome;
    
    @CreatedDate
    private LocalDateTime dataCriacao;
    
    @LastModifiedDate
    private LocalDateTime dataAtualizacao;
}
```

**Configuração necessária:**
```properties
spring.jpa.properties.org.hibernate.envers.audit_table_suffix=_aud
```

### Documentação OpenAPI/Swagger
O framework inclui suporte a documentação automática via SpringDoc OpenAPI:

**Acessar a documentação:**
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

**Configuração:**
A classe `OpenApiConfig` já está incluída e configurada com informações básicas do projeto.

### Suporte a DTOs
O framework suporta duas abordagens para trabalhar com DTOs:

#### Opção 1 - Uso Direto em Entidades (Padrão)
Por padrão, o framework expõe entidades diretamente. Para maior controle, use `@JsonIgnore` e `@ReadOnly`:

```java
@Entity
@FasterCRUD(path = "/produtos")
public class Produto {
    @Id
    private Long id;
    
    private String nome;
    
    @JsonIgnore
    private String senha;  // Não exposto na API
    
    @ReadOnly
    private String codigoInterno;  // Exposto mas não editável
}
```

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

**Vantagens de usar DTOs:**
- Controle total sobre quais campos são expostos
- Separação clara entre modelo de domínio e API
- Possibilidade de transformar dados antes de expor
- Maior segurança para APIs públicas

**Comportamento:**
- Requisições POST/PUT/PATCH recebem DTO → mapeado para Entidade → salvo
- Respostas GET retornam Entidade → mapeado para DTO → JSON
- Apenas campos com mesmo nome são mapeados automaticamente

## Dependências

O framework requer as seguintes dependências (já incluídas no pom.xml):

**Core:**
- Spring Boot 3.2.5+
- Spring Data JPA 3.2.5+
- Jackson Databind 2.15.2+
- Spring Boot Starter Validation
- Jakarta Persistence API 3.1.0+

**Funcionalidades Avançadas:**
- Spring Data Envers 3.2.5+ (para auditoria)
- SpringDoc OpenAPI Starter WebMVC UI 2.3.0+ (para documentação Swagger)
- MapStruct 1.5.5.Final (para mapeamento DTO ↔ Entity)

**Testes:**
- Spring Boot Starter Test
- Mockito Inline 5.2.0+ (para mocking no Java 21+)

## Executando o exemplo

O projeto inclui um exemplo de uso com a entidade `Produto`. Para executar:

```bash
mvn spring-boot:run
```

A aplicação iniciará em http://localhost:8080 com o banco H2 em memória.

Teste os endpoints:

```bash
# Criar produto
curl -X POST http://localhost:8080/produtos \
  -H "Content-Type: application/json" \
  -d '{"nome":"Notebook","preco":3500.00,"descricao":"Notebook Dell"}'

# Listar produtos
curl http://localhost:8080/produtos

# Buscar por ID
curl http://localhost:8080/produtos/1

# Atualizar parcialmente
curl -X PATCH http://localhost:8080/produtos/1 \
  -H "Content-Type: application/json" \
  -d '{"preco":3200.00}'
```

## Testes

Execute os testes com:

```bash
mvn test
```

O projeto inclui:
- Testes unitários para `GenericCrudService` (validação)
- Testes de integração para o fluxo completo CRUD (20 testes)
- Testes para paginação, ordenação, filtros e campos readonly

## Exemplos Avançados

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
Use `@CreatedDate` e `@LastModifiedDate` para rastrear alterações:

```java
@CreatedDate
@Column(updatable = false)
private LocalDateTime dataCriacao;

@LastModifiedDate
private LocalDateTime dataAtualizacao;
```

## Troubleshooting

### Erro: "Name for argument of type [java.lang.Integer] not specified"

**Causa:** O compilador não está preservando nomes de parâmetros.

**Solução:** Adicione a flag `-parameters` ao compilador Maven:

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <configuration>
        <parameters>true</parameters>
    </configuration>
</plugin>
```

### Erro: "No qualifying bean of type EntityManager"

**Causa:** O EntityManager não está sendo injetado corretamente.

**Solução:** Verifique se você tem um datasource configurado:

```properties
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driver-class-name=org.h2.Driver
spring.jpa.hibernate.ddl-auto=create-drop
```

### Filtros numéricos não funcionam

**Causa:** Operadores `gt`, `lt`, `gte`, `lte` só funcionam em campos do tipo `Number`.

**Solução:** Certifique-se de que o campo é um tipo numérico:

```java
// Correto
private Integer quantidade;
private Long preco;
private BigDecimal valor;

// Incorreto para operadores numéricos
private String preco;  // Use gt/lt/gte/lte apenas em Number
```

### Campos @ReadOnly ainda estão sendo modificados

**Causa:** A annotation `@ReadOnly` não está sendo reconhecida.

**Solução:** Verifique se a annotation está no pacote correto e se foi importada:

```java
import annotation.ReadOnly;

@ReadOnly
private String codigoInterno;
```

## Limitações

- **Transações complexas:** O framework usa transações simples. Para lógica de negócio complexa, considere criar Services customizados.
- **DTOs:** O framework expõe entidades diretamente. Para APIs públicas, considere usar DTOs e mapeamento manual.
- **Segurança:** O framework não inclui autenticação/autorização. Use Spring Security para proteger endpoints.
- **Performance:** Para queries complexas, considere criar repositories customizados com `@Query`.

## Roadmap

Funcionalidades planejadas para versões futuras:

- [ ] **Suporte a DTOs (Abordagem Híbrida)**
  - **Opção 1 - Geração automática:** Se `@FasterCRUD` for usado na entidade, gerar DTO automaticamente em build-time
    - Respeita `@JsonIgnore` (não inclui no DTO)
    - Respeita `@ReadOnly` (inclui mas marca como não-editável)
    - Usa MapStruct ou similar para mapeamento
  - **Opção 2 - DTO manual:** Se `@FasterCRUD` for usado em um DTO, mapear para entidade automaticamente
    - Usuário tem controle total sobre estrutura do DTO
    - Annotation `@EntityMapping` para especificar entidade alvo
    - Mapeamento automático via reflection ou MapStruct
  - **Benefício:** Flexibilidade máxima - usuários simples usam geração automática, usuários avançados usam DTOs customizados

## Contribuindo

Contribuições são bem-vindas! Sinta-se à vontade para:

1. Fork o projeto
2. Criar uma branch para sua feature (`git checkout -b feature/nova-funcionalidade`)
3. Commit suas mudanças (`git commit -m 'Adiciona nova funcionalidade'`)
4. Push para a branch (`git push origin feature/nova-funcionalidade`)
5. Abrir um Pull Request

## Licença

Este projeto está sob a licença MIT. Veja o arquivo LICENSE para mais detalhes.

## Suporte

Para problemas, dúvidas ou sugestões:

- Abra uma issue no GitHub
- Consulte a documentação do Spring Boot
- Consulte a documentação do Spring Data JPA
