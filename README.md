# FasterAPI - Framework Automatizado de CRUD

## 🚀 Visão Geral

O **FasterAPI** é um framework Java/Spring Boot revolucionário que automatiza a criação de endpoints RESTful CRUD (Create, Read, Update, Delete) usando anotações customizadas e reflexão. Com apenas uma anotação na sua entidade JPA, você ganha instantaneamente uma API completa com paginação, filtragem e suporte a DTOs.

## 📋 Índice

- [🎯 Objetivo](#-objetivo)
- [✨ Funcionalidades](#-funcionalidades)
- [🏗️ Arquitetura](#️-arquitetura)
- [📦 Dependências](#-dependências)
- [🚀 Guia de Instalação](#-guia-de-instalação)
- [📖 Guia de Uso](#-guia-de-uso)
- [🔧 Configuração Avançada](#-configuração-avançada)
- [📝 Exemplos Práticos](#-exemplos-práticos)
- [🐛 Troubleshooting](#-bug-troubleshooting)
- [🤝 Contribuição](#-contribuição)
- [📄 Licença](#-licença)

## 🎯 Objetivo

O FasterAPI foi desenvolvido para eliminar o trabalho repetitivo de criar endpoints CRUD básicos em aplicações Spring Boot. Em vez de escrever Controllers, Services e Repositories manualmente para cada entidade, basta anotar sua classe e o framework faz todo o trabalho pesado.

### Problemas Resolvidos

- ✅ **Elimina código boilerplate** - Não mais Controllers e Services repetitivos
- ✅ **Padronização automática** - Todas as APIs seguem o mesmo padrão RESTful
- ✅ **Configuração mínima** - Apenas uma anotação necessária
- ✅ **Flexibilidade máxima** - Suporte a DTOs, paginação e filtros
- ✅ **Manutenibilidade** - Mudanças em um lugar afetam toda a API

## ✨ Funcionalidades

### Funcionalidades Principais

| Funcionalidade | Descrição | Status |
|---------------|-----------|--------|
| 🔄 **CRUD Automático** | Geração automática de endpoints Create, Read, Update, Delete | ✅ Completo |
| 📄 **Paginação** | Suporte nativo a paginação com parâmetros `page`, `size`, `sort` | ✅ Completo |
| 🔍 **Filtragem Dinâmica** | Filtros automáticos baseados nos campos da entidade | ✅ Completo |
| 🎭 **Suporte a DTOs** | Mapeamento automático para DTOs personalizados | ✅ Completo |
| 🏗️ **Reflexão Avançada** | Descoberta e registro dinâmico de entidades | ✅ Completo |
| 📊 **Respostas Padronizadas** | Formato consistente de respostas HTTP | ✅ Completo |

### Funcionalidades Técnicas

- **Spring Boot 6.1.5** - Última versão estável do Spring Boot
- **Jakarta EE 10** - Migrado para o padrão Jakarta (não mais javax)
- **JPA/Hibernate** - Persistência de dados com Spring Data JPA
- **Especificações JPA** - Queries dinâmicas com Specification pattern
- **Injeção de Dependências** - Integração completa com o container Spring

## 🏗️ Arquitetura

### Diagrama de Componentes

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   @FasterCRUD   │───▶│  CrudRegistrar   │───▶│ Spring Context  │
│   (Entidade)    │    │ (Scanner/Registry)│    │                 │
└─────────────────┘    └──────────────────┘    └─────────────────┘
         │                       │                       │
         ▼                       ▼                       ▼
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│ GenericCrudCtrl  │◀───│ GenericCrudSvc   │◀───│ JpaRepository   │
│ (Endpoints)     │    │ (Lógica de Neg.)  │    │ (Dados)         │
└─────────────────┘    └──────────────────┘    └─────────────────┘
         │                       │                       │
         ▼                       ▼                       ▼
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│  DtoMapper      │◀───│SpecificationBldr │◀───│   EntityManager │
│ (Conversão)     │    │ (Filtros)        │    │ (JPA)           │
└─────────────────┘    └──────────────────┘    └─────────────────┘
```

### Camadas da Arquitetura

#### 1. Camada de Anotações (`annotation/`)
- **`@FasterCRUD`** - Anotação principal que marca entidades para geração automática de CRUD
- **`@CrudDTO`** - Anotação opcional para definir DTOs personalizados

#### 2. Camada de Core (`core/`)
- **`CrudRegistrar`** - Responsável por escanear e registrar entidades anotadas
- **`GenericCrudController`** - Controller genérico que implementa os endpoints REST
- **`GenericCrudService`** - Service genérico com lógica de negócio CRUD
- **`DtoMapper`** - Utilitário para conversão entre Entidades e DTOs
- **`SpecificationExecutorRepository`** - Interface customizada com JPA+Specification

#### 3. Camada de Filtros (`filter/`)
- **`SpecificationBuilder`** - Construtor dinâmico de especificações JPA para filtragem

## 📦 Dependências

### Dependências Maven Necessárias

```xml
<properties>
    <maven.compiler.source>22</maven.compiler.source>
    <maven.compiler.target>22</maven.compiler.target>
    <spring.version>6.1.5</spring.version>
</properties>

<dependencies>
    <!-- Spring Core -->
    <dependency>
        <groupId>org.springframework</groupId>
        <artifactId>spring-context</artifactId>
        <version>${spring.version}</version>
    </dependency>
    
    <!-- Spring Web -->
    <dependency>
        <groupId>org.springframework</groupId>
        <artifactId>spring-web</artifactId>
        <version>${spring.version}</version>
    </dependency>
    
    <!-- Spring WebMVC -->
    <dependency>
        <groupId>org.springframework</groupId>
        <artifactId>spring-webmvc</artifactId>
        <version>${spring.version}</version>
    </dependency>
    
    <!-- Spring Data JPA -->
    <dependency>
        <groupId>org.springframework.data</groupId>
        <artifactId>spring-data-jpa</artifactId>
        <version>3.2.5</version>
    </dependency>
    
    <!-- Jakarta Persistence API -->
    <dependency>
        <groupId>jakarta.persistence</groupId>
        <artifactId>jakarta.persistence-api</artifactId>
        <version>3.1.0</version>
    </dependency>
    
    <!-- Jakarta Servlet API -->
    <dependency>
        <groupId>jakarta.servlet</groupId>
        <artifactId>jakarta.servlet-api</artifactId>
        <version>6.0.0</version>
        <scope>provided</scope>
    </dependency>
    
    <!-- Spring Transactions -->
    <dependency>
        <groupId>org.springframework</groupId>
        <artifactId>spring-tx</artifactId>
        <version>${spring.version}</version>
    </dependency>
</dependencies>
```

### Requisitos de Sistema

- **Java 22+** - O projeto está configurado para Java 22
- **Maven 3.6+** - Para gerenciamento de dependências
- **Servlet Container** - Tomcat, Jetty ou Undertow (geralmente embarcado)
- **Banco de Dados** - Qualquer banco compatível com JPA/Hibernate

## 🚀 Guia de Instalação

### Passo 1: Configurar o Projeto

1. **Clone ou crie um projeto Maven**
2. **Configure o `pom.xml`** com as dependências listadas acima
3. **Configure o banco de dados** no `application.properties`

### Passo 2: Configurar Banco de Dados

```properties
# application.properties
spring.datasource.url=jdbc:mysql://localhost:3306/seu_banco
spring.datasource.username=seu_usuario
spring.datasource.password=sua_senha
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
```

### Passo 3: Habilitar o Framework

Crie uma classe de configuração para habilitar o FasterAPI:

```java
@Configuration
@EnableTransactionManagement
public class FasterApiConfig {
    
    @Bean
    public CrudRegistrar crudRegistrar() {
        return new CrudRegistrar("com.seu.pacote.entities");
    }
}
```

## 📖 Guia de Uso

### Uso Básico - Entidade Simples

1. **Crie sua entidade JPA** com a anotação `@FasterCRUD`:

```java
@Entity
@Table(name = "produtos")
@FasterCRUD(path = "produtos")
public class Produto {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String nome;
    
    @Column
    private String descricao;
    
    @Column(nullable = false)
    private BigDecimal preco;
    
    // Getters e Setters
}
```

2. **Pronto!** Sua API está criada com os seguintes endpoints:

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/api/produtos` | Listar todos (com paginação e filtros) |
| GET | `/api/produtos/{id}` | Buscar por ID |
| POST | `/api/produtos` | Criar novo |
| PUT | `/api/produtos/{id}` | Atualizar existente |
| DELETE | `/api/produtos/{id}` | Deletar |

### Exemplos de Requisições

#### Listar Produtos (com paginação)
```bash
GET /api/produtos?page=0&size=10&sort=nome
```

#### Filtrar Produtos
```bash
GET /api/produtos?nome=celular&preco=1000
```

#### Criar Produto
```bash
POST /api/produtos
Content-Type: application/json

{
  "nome": "Smartphone XYZ",
  "descricao": "Smartphone de última geração",
  "preco": 2999.99
}
```

#### Atualizar Produto
```bash
PUT /api/produtos/1
Content-Type: application/json

{
  "nome": "Smartphone XYZ Pro",
  "preco": 3299.99
}
```

### Uso Avançado - com DTOs

1. **Crie sua classe DTO**:

```java
public class ProdutoDTO {
    private Long id;
    private String nome;
    private String precoFormatado;
    
    // Construtor vazio necessário para reflexão
    public ProdutoDTO() {}
    
    // Getters e Setters
}
```

2. **Anote a entidade com `@CrudDTO`**:

```java
@Entity
@Table(name = "produtos")
@FasterCRUD(path = "produtos")
@CrudDTO(ProdutoDTO.class)
public class Produto {
    // ... campos da entidade
}
```

3. **O DtoMapper converterá automaticamente** a entidade para o DTO nas respostas.

## 🔧 Configuração Avançada

### Parâmetros da Anotação @FasterCRUD

```java
@FasterCRUD(
    path = "produtos",           // Caminho base da API (obrigatório)
    name = "produto",            // Nome customizado (opcional)
    pageable = true,             // Habilita paginação (default: true)
    filterable = true            // Habilita filtros (default: true)
)
```

### Configuração de Paginação

A paginação é automática e suporta os seguintes parâmetros:

- **`page`** - Número da página (inicia em 0)
- **`size`** - Tamanho da página (default: 10)
- **`sort`** - Campo para ordenação

Exemplo:
```bash
GET /api/produtos?page=1&size=20&sort=preco,desc
```

### Configuração de Filtros

Todos os campos da entidade são filtráveis automaticamente:

```bash
# Filtra por múltiplos campos
GET /api/produtos?nome=smartphone&descricao=128gb

# Filtros são case-insensitive e usam LIKE
GET /api/produtos?nome=galaxy  # Encontra "Galaxy S21", "Galaxy Note", etc.
```

### Campos Reservados

Os seguintes parâmetros são reservados para controle:

- `page` - Controle de paginação
- `size` - Tamanho da página  
- `sort` - Ordenação

## 📝 Exemplos Práticos

### Exemplo 1: Sistema de E-commerce

```java
@Entity
@Table(name = "categorias")
@FasterCRUD(path = "categorias", name = "categoria")
public class Categoria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String nome;
    
    @Column
    private String descricao;
    
    // Getters e Setters
}

@Entity
@Table(name = "clientes")
@FasterCRUD(path = "clientes")
@CrudDTO(ClienteDTO.class)
public class Cliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String nome;
    
    @Column(nullable = false, unique = true)
    private String email;
    
    @Column
    private String telefone;
    
    // Getters e Setters
}
```

### Exemplo 2: Sistema de Blog

```java
@Entity
@Table(name = "posts")
@FasterCRUD(path = "posts", pageable = true, filterable = true)
@CrudDTO(PostDTO.class)
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String titulo;
    
    @Column(columnDefinition = "TEXT")
    private String conteudo;
    
    @Column(name = "data_criacao")
    private LocalDateTime dataCriacao;
    
    @Column(name = "data_atualizacao")
    private LocalDateTime dataAtualizacao;
    
    @PrePersist
    protected void onCreate() {
        dataCriacao = LocalDateTime.now();
        dataAtualizacao = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        dataAtualizacao = LocalDateTime.now();
    }
    
    // Getters e Setters
}
```

### Exemplo 3: DTOs Complexos

```java
// Entidade completa
@Entity
@Table(name = "pedidos")
@FasterCRUD(path = "pedidos")
@CrudDTO(PedidoDTO.class)
public class Pedido {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String cliente;
    
    @Column(nullable = false)
    private BigDecimal total;
    
    @Column(name = "data_pedido")
    private LocalDateTime dataPedido;
    
    @Enumerated(EnumType.STRING)
    private StatusPedido status;
    
    // Getters e Setters
}

// DTO simplificado
public class PedidoDTO {
    private Long id;
    private String cliente;
    private String totalFormatado;
    private String dataFormatada;
    private String status;
    
    public PedidoDTO() {}
    
    // Getters e Setters
}
```

## 🐛 Troubleshooting

### Problemas Comuns

#### 1. "Cannot resolve symbol 'HttpServletRequest'"
**Causa:** Falta da dependência `jakarta.servlet-api`
**Solução:** Adicione a dependência no pom.xml:
```xml
<dependency>
    <groupId>jakarta.servlet</groupId>
    <artifactId>jakarta.servlet-api</artifactId>
    <version>6.0.0</version>
    <scope>provided</scope>
</dependency>
```

#### 2. Entidades não são detectadas
**Causa:** Pacote base incorreto no CrudRegistrar
**Solução:** Verifique se o pacote está correto:
```java
@Bean
public CrudRegistrar crudRegistrar() {
    return new CrudRegistrar("com.seuprojeto.entities"); // Pacote correto
}
```

#### 3. Filtros não funcionam
**Causa:** Nomes de campos incorretos ou campos não existentes
**Solução:** Verifique se os nomes dos campos na URL correspondem exatamente aos campos da entidade.

#### 4. Erro de conversão de DTO
**Causa:** DTO não tem construtor padrão ou campos não correspondem
**Solução:** Garanta que o DTO tenha:
- Construtor público sem argumentos
- Campos com os mesmos nomes da entidade

### Debug e Logs

Habilite logs para debug:

```properties
# application.properties
logging.level.core=DEBUG
logging.level.annotation=DEBUG
logging.level.filter=DEBUG
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

### Performance

#### Otimizações Recomendadas

1. **Índices no Banco**: Crie índices para campos frequentemente filtrados
2. **DTOs para Entidades Grandes**: Use DTOs para reduzir o tamanho das respostas
3. **Paginação**: Sempre use paginação para grandes volumes de dados
4. **Cache**: Considere cache para dados frequentemente acessados

## 🤝 Contribuição

### Como Contribuir

1. **Fork** o projeto
2. **Crie uma branch** para sua feature: `git checkout -b feature/nova-funcionalidade`
3. **Commit** suas mudanças: `git commit -m 'Adicionando nova funcionalidade'`
4. **Push** para a branch: `git push origin feature/nova-funcionalidade`
5. **Abra um Pull Request**

### Diretrizes de Contribuição

- **Code Style**: Siga os padrões Java e Spring Boot
- **Testes**: Adicione testes unitários para novas funcionalidades
- **Documentação**: Atualize a documentação para mudanças significativas
- **Commits**: Use mensagens de commit claras e descritivas

### Estrutura do Projeto para Contribuidores

```
src/
├── main/
│   ├── java/
│   │   ├── annotation/     # Anotações customizadas
│   │   ├── core/          # Classes principais do framework
│   │   ├── filter/        # Filtros e especificações
│   │   └── config/        # Configurações do Spring
│   └── resources/
│       └── application.properties
├── test/
│   └── java/              # Testes unitários e de integração
└── docs/                  # Documentação adicional
```

## 📄 Licença

Este projeto está licenciado sob a **Licença Creative Commons Attribution-NonCommercial 4.0 International (CC BY-NC 4.0)**.

### O que você PODE fazer:

- ✅ **Uso pessoal e educacional** - Use para aprendizado e projetos pessoais
- ✅ **Modificação** - Modifique o código conforme necessário
- ✅ **Distribuição** - Compartilhe cópias com outros
- ✅ **Atribuição** - Dê crédito ao autor original

### O que você NÃO PODE fazer:

- ❌ **Uso comercial** - Não é permitido uso em projetos comerciais sem licença específica
- ❌ **Venda** - Não pode vender o framework ou serviços baseados nele
- ❌ **Sublicenciamento comercial** - Não pode criar licenças comerciais derivadas

### Condições Gerais:

- ⚠️ **Inclua o copyright** - Mantenha a nota de copyright original
- ⚠️ **Inclua a licença** - Distribua com o arquivo de licença
- ⚠️ **Indique alterações** - Marque claramente as modificações feitas

## 📞 Suporte

### Canais de Suporte

- **Issues GitHub**: Reporte bugs e solicite funcionalidades
- **Documentação**: Consulte este README e os JavaDocs
- **Comunidade**: Discord (em breve)

### Tempo de Resposta (SLA)

| Tipo de Solicitação | Tempo de Resposta | Prioridade |
|-------------------|------------------|------------|
| **Bugs Críticos** | 5-10 dias úteis | Alta |
| **Dúvidas Gerais** | 7-14 dias úteis | Média |
| **Novas Funcionalidades** | Roadmap trimestral | Baixa |
| **Licenças Comerciais** | 24-48 horas | Alta |


## 🗺️ Roadmap Futuro

### Versão 1.1.0 (Planejada)
- 🎭 **Autodocumentação com Swagger/OpenAPI** - Geração automática de documentação de API
- 📊 **Padronização de Respostas** - Formato consistente para todas as respostas da API
- 🔐 **Melhorias de Segurança** - Validação e sanitização de dados
- ⚡ **Performance** - Cache integrado e otimizações de queries

## 🎉 Conclusão

O FasterAPI revoluciona o desenvolvimento de APIs RESTful em Java, eliminando o trabalho repetitivo e permitindo que os desenvolvedores foquem na lógica de negócio real. Com configuração mínima e máxima flexibilidade, é a ferramenta perfeita para acelerar o desenvolvimento de aplicações Spring Boot.

**Pronto para acelerar seu desenvolvimento? Comece a usar o FasterAPI hoje mesmo! 🚀**

---

*Última atualização: Março 2026*
*Versão: 1.0.0*
*Licença: CC BY-NC 4.0*
