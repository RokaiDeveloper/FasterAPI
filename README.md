# FasterCRUD

Automatize endpoints REST para entidades JPA com uma anotação.

## Instalação

1. Crie a estrutura de pacotes `fasterapi` e copie os arquivos do framework:

```
src/main/java/
└── fasterapi/
    ├── annotation/
    │   └── FasterCRUD.java
    └── core/
        ├── FasterCrudInitializer.java
        ├── GenericCrudService.java
        └── GenericCrudController.java
```

2. Na sua classe principal (`@SpringBootApplication`), adicione o pacote `fasterapi` ao `@ComponentScan`:

```java
@SpringBootApplication
@ComponentScan(basePackages = {
    "com.seuprojeto",
    "fasterapi"
})
public class SuaApplication {
    public static void main(String[] args) {
        SpringApplication.run(SuaApplication.class, args);
    }
}
```

3. No arquivo `application.properties`, defina o pacote onde estão suas entidades:

```properties
fasterapi.base-package=com.seuprojeto.models
```

4. **Validação da instalação**: Ao iniciar a aplicação, verifique os logs para confirmar que o framework foi inicializado corretamente. Você deve ver mensagens como:

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
logging.level.fasterapi.core.FasterCrudInitializer=DEBUG
```

Com isso você verá logs adicionais mostrando o registro de cada método endpoint.

## Uso

Anote qualquer entidade JPA com `@FasterCRUD`:

```java
@Entity
@FasterCRUD(path = "/clientes")
public class Cliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
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

# Atualizar
curl -X PUT http://localhost:8080/clientes/1 \
  -H "Content-Type: application/json" \
  -d '{"nome":"João Silva"}'

# Deletar
curl -X DELETE http://localhost:8080/clientes/1
```
