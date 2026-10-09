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
    <artifactId>fasterapi-spring-boot-starter</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

O starter é carregado automaticamente pelo Spring Boot. Não é necessário
copiar classes do framework nem declarar `@ComponentScan` para os pacotes
internos.

3. No arquivo `application.properties`, defina o pacote onde estão suas entidades:

```properties
fasterapi.base-package=com.seuprojeto.models
```

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

## Publicação Maven

Para testar o artefato no repositório local:

```bash
./mvnw clean install
```

Para publicar no Maven Central, primeiro é necessário criar e verificar o
namespace `com.rokaidev` no Sonatype Central Portal, configurar um token no
`~/.m2/settings.xml` com o id `central` e configurar uma chave GPG local.
Depois, use uma versão final (sem `SNAPSHOT`) e execute:

```bash
./mvnw clean deploy -Prelease -Dgpg.keyname=SEU_ID_GPG
```

O profile `release` gera os fontes, Javadoc e assinaturas. O plugin de
publicação usa o servidor Maven com id `central`; nenhuma credencial deve ser
armazenada no `pom.xml` ou no repositório.

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

As operações podem ser habilitadas individualmente. Uma operação desabilitada não
é registrada no Spring MVC e retorna 404:

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

O endpoint `/v3/api-docs` e o Swagger UI refletem os mappings efetivamente
registrados no Spring MVC. Portanto, operações desabilitadas não aparecem na
documentação nem ficam disponíveis como endpoints.

Para cada recurso, o OpenAPI gera:

- Uma tag com o nome da entidade ou DTO anotado com `@FasterCRUD`
- Schema de resposta `<Recurso>Response`
- Schema de entrada `<Recurso>Request` para POST, PUT e PATCH
- Tipos OpenAPI para strings, enums, booleanos, inteiros, números e datas
- Respostas `200`, `201`, `204`, `400` e `404` conforme a operação

Schemas de entrada não incluem o identificador nem campos marcados com
`@ReadOnly`. Campos marcados com `@JsonIgnore` não são expostos nos schemas de
resposta nem nos filtros documentados.

O GET de listagem documenta os parâmetros:

- `page`: página iniciando em `0`
- `size`: quantidade de registros por página, com padrão `10`
- `sort`: `campo,direcao`, usando `asc` ou `desc`
- Filtros por igualdade, como `nome=Notebook` ou `nome__eq=Notebook`
- `campo__like`, `campo__gt`, `campo__lt`, `campo__gte` e `campo__lte`

Os operadores `gt`, `lt`, `gte` e `lte` são aplicáveis a campos numéricos,
conforme o comportamento do serviço.

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

A herança é gerenciada pelo provedor JPA do projeto cliente. O CRUD automático
deve ser colocado na raiz concreta do modelo somente quando o contrato de
serialização e o identificador forem adequados à API.

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

O JPA incrementa o version e trata conflitos conforme a configuração do
projeto. O FasterAPI não transforma exceções de concorrência em um contrato
HTTP específico.

### Auditoria

Auditoria não é incluída no starter. Caso a aplicação use Spring Data
Auditing, Envers ou outro mecanismo, adicione e configure essas dependências
no projeto cliente. Campos de auditoria expostos no CRUD seguem as mesmas
regras de serialização e escrita da entidade, salvo uso de `@ReadOnly` ou
`@JsonIgnore`.

### Documentação OpenAPI/Swagger
O framework integra automaticamente com SpringDoc OpenAPI quando a dependência
opcional está presente. Para habilitar Swagger no projeto consumidor, adicione:

```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.3.0</version>
</dependency>
```

Com SpringDoc presente, o framework fornece documentação automática via OpenAPI:

Por padrão, a integração é ativada somente quando SpringDoc está disponível no
classpath. Ela pode ser controlada explicitamente:

```properties
# padrão: true
fasterapi.openapi.enabled=true
```

Para manter o CRUD ativo sem registrar a personalização Swagger:

```properties
fasterapi.openapi.enabled=false
```

Se o projeto cliente não incluir SpringDoc, o FasterAPI continua funcionando
normalmente; apenas `/swagger-ui.html` e `/v3/api-docs` não serão fornecidos
pelo framework.

As rotas do SpringDoc também podem ser personalizadas pelo projeto cliente,
usando as propriedades oficiais do SpringDoc:

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

O FasterAPI não cria uma rota própria para o Swagger. Ele registra os CRUDs na
instância OpenAPI gerenciada pelo SpringDoc, portanto a documentação aparece
automaticamente na rota personalizada escolhida pelo cliente. A aplicação
também pode desabilitar apenas a interface visual:

```properties
springdoc.swagger-ui.enabled=false
```

**Acessar a documentação:**
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

**Configuração:**
A classe `OpenApiConfig` já está incluída e configurada com informações básicas do projeto.
Não é necessário criar controllers estáticos apenas para que os CRUDs gerados
apareçam no Swagger.

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
- O OpenAPI usa os campos do DTO manual para gerar os schemas e filtros, sem
  documentar campos adicionais existentes somente na entidade

## Dependências

O starter é destinado a aplicações Spring Boot 3.x com Java 21 ou superior e
integra Spring MVC Servlet, Spring Data JPA, Jakarta Persistence, Jakarta Bean
Validation e Jackson.

O consumidor precisa fornecer o driver e a configuração do banco de dados. O
starter não inclui banco de dados de produção e não impõe H2 à aplicação.

SpringDoc é opcional. Para habilitar a documentação, adicione a dependência
descrita na seção [Documentação OpenAPI/Swagger](#documentação-openapiswagger).

O FasterAPI não usa MapStruct nem Spring Data Envers. Auditoria, autenticação,
autorização e regras de negócio continuam sendo responsabilidades da aplicação.

## Testes do projeto

Execute os testes com:

```bash
./mvnw test
```

Para validar o artefato Maven completo:

```bash
./mvnw clean test package
```

A suíte cobre o serviço CRUD, entidades JPA, DTOs, paginação, ordenação,
filtros, `@ReadOnly`, operações seletivas e documentação OpenAPI.

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

O FasterAPI não implementa auditoria. Para usar `@CreatedDate`,
`@LastModifiedDate`, Envers ou outro mecanismo de histórico, configure-o no
projeto cliente e mantenha esses campos fora das operações automáticas quando
a aplicação exigir regras específicas.

## Troubleshooting

### A aplicação inicia, mas nenhum CRUD é registrado

O scanner usa `fasterapi.base-package` como raiz e procura classes anotadas
com `@FasterCRUD`; ele não procura apenas classes marcadas com `@Entity`.
Confirme que o pacote configurado contém as entidades no classpath e habilite
o log para acompanhar o registro:

```properties
logging.level.com.rokaidev.fasterapi=DEBUG
```

### O contexto falha ao criar o FasterAPI

O starter é condicionado a uma aplicação web Servlet com JPA disponível.
WebFlux, JDBC puro ou uma aplicação sem `EntityManager` não atendem ao
contrato atual. O projeto cliente também precisa fornecer datasource,
provedor JPA e entidades com `@Id`; os identificadores CRUD atuais são
tratados como `Long`.

### Existe conflito de mapping ao iniciar

Os mappings são registrados dinamicamente no startup. O conflito ocorre quando
dois recursos usam a mesma combinação de caminho e método HTTP, ou quando uma
rota da aplicação coincide com uma rota CRUD gerada. Use caminhos únicos em
`@FasterCRUD(path = "...")` e não crie um segundo controller para a mesma URL.

### O Swagger não aparece

A documentação só é criada quando o projeto cliente inclui
`springdoc-openapi-starter-webmvc-ui` e `fasterapi.openapi.enabled` não está
definido como `false`. Verifique também se o cliente não desativou:

```properties
springdoc.api-docs.enabled=false
springdoc.swagger-ui.enabled=false
```

Quando as rotas foram personalizadas, use os caminhos configurados em
`springdoc.api-docs.path` e `springdoc.swagger-ui.path`.

### A documentação mostra schema ou filtros inesperados

Para DTO automático, a documentação usa os campos da entidade que não possuem
`@JsonIgnore`. Para DTO manual, usa os campos do tipo anotado com
`@FasterCRUD(isDto = true)`, não todos os campos da entidade mapeada.
`@ReadOnly` é removido dos schemas de entrada e `@JsonIgnore` remove campos da
exposição e dos filtros documentados.

### Um filtro retorna resultado vazio ou não altera a consulta

O nome precisa corresponder ao campo Java da entidade ou DTO exposto.
`gt`, `lt`, `gte` e `lte` são operadores numéricos; `like` é uma busca textual
parcial. Campos desconhecidos são ignorados pelo serviço, portanto um erro de
digitação pode parecer uma consulta sem filtro. Ao combinar filtros, todos os
predicados são aplicados conjuntamente.

### A resposta de listagem tem formato diferente

Sem `page`, `size` e `sort`, a listagem retorna uma coleção JSON. Quando
qualquer parâmetro de paginação ou ordenação é enviado, retorna um objeto
paginado com `content`, `totalElements`, `totalPages`, `number`, `size`,
`first` e `last`. Esse comportamento deve ser refletido no cliente gerado ou
no contrato OpenAPI consumido.

## Limitações

- **Stack:** o suporte atual é Spring MVC Servlet + Spring Data JPA; WebFlux,
  JDBC puro, MongoDB e R2DBC não são adaptadores suportados.
- **Identificador:** os identificadores CRUD são tratados como `Long`.
- **AOT/native:** o scanner e os mappings são configurados em runtime;
  aplicações nativas/AOT podem exigir uma integração específica de runtime
  hints.
- **Domínio:** o serviço não substitui regras de negócio, autorização ou
  transações compostas.
- **Relacionamentos:** DTOs próprios podem ser necessários para evitar ciclos,
  lazy loading inesperado ou respostas extensas.
- **Filtros:** filtros dinâmicos são baseados em campos diretos; consultas
  complexas devem usar endpoints ou repositórios específicos.

## Contribuindo

Contribuições são bem-vindas! Sinta-se à vontade para:

1. Fork o projeto
2. Criar uma branch para sua feature (`git checkout -b feature/nova-funcionalidade`)
3. Commit suas mudanças (`git commit -m 'Adiciona nova funcionalidade'`)
4. Push para a branch (`git push origin feature/nova-funcionalidade`)
5. Abrir um Pull Request

## Licença

Este projeto está sob a FasterAPI Proprietary Free-Use License. O framework
pode ser usado como dependência em aplicações pessoais e comerciais, mas o
código do FasterAPI não pode ser copiado, modificado, redistribuído ou usado
para criar versões derivadas sem autorização escrita. Veja o arquivo LICENSE
para os termos completos.

## Suporte

Para problemas, dúvidas ou sugestões:

- Abra uma issue no GitHub
- Consulte a documentação do Spring Boot
- Consulte a documentação do Spring Data JPA
