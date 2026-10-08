# FasterCRUD

Automatize endpoints REST para entidades JPA com uma anotação.

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

- **Conversão automática de tipos**: O framework usa Jackson ObjectMapper para converter automaticamente valores JSON para os tipos corretos dos campos da entidade (String, Integer, BigDecimal, etc.)
- **Validação**: Suporte a anotações de validação Jakarta Validation (@NotNull, @NotBlank, @Positive, etc.)
- **Tratamento de erros**: Respostas HTTP apropriadas:
  - 400 Bad Request para erros de validação
  - 404 Not Found para entidades não encontradas
  - 204 No Content para deleções bem-sucedidas
  - 201 Created para criações bem-sucedidas

## Dependências

O framework requer as seguintes dependências (já incluídas no pom.xml):

- Spring Boot 3.2.5+
- Spring Data JPA 3.2.5+
- Jackson Databind 2.15.2+
- Spring Boot Starter Validation
- Jakarta Persistence API 3.1.0+

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
- Testes unitários para `GenericCrudService`
- Testes de integração para o fluxo completo CRUD
