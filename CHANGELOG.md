# Changelog

All notable changes to FasterAPI are documented in this file.

The format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and the project adheres to [Semantic Versioning](https://semver.org/).

**Português (Brasil):** [Ver versão em português](#português-brasil)

## [1.0.5] - 2026-10-16

### Fixed

- Fixed automatic registration of `FasterFilterAutoConfiguration` through Spring Boot auto-configuration imports.
- Fixed argument resolution for `FasterFilterQuery<T>` in custom controllers.
- Fixed repository initialization for `FasterFilterQuery<T>` queries, preventing `NullPointerException` errors when executing `findAll()` and `findAll(Pageable)`.

### Tests

- Added regression coverage for automatic filter configuration registration and repository initialization in custom-controller queries.

## [1.0.4] - 2026-10-15

### Added

- Added `FasterFilterQuery<T>` as a high-level facade for custom controllers, hiding `FasterFilters`, JPA `Specification`, and repository implementation details.
- Added support for executing filtered results directly with `findAll()` and `findAll(Pageable)`.

### Fixed

- Fixed numeric predicates for the `gt`, `lt`, `gte`, and `lte` operators.
- Fixed strict validation for unsupported fields, operators, incompatible types, and invalid numeric values.
- Preserved `page`, `size`, and `sort` as resolver-excluded parameters.

### Tests

- Added integration coverage for all comparison operators, invalid filter values, strict validation, ignored pagination parameters, and the simplified `FasterFilterQuery<T>` API.

### Documentation

- Added custom-controller examples using the simplified `com.rokaidev.fasterapi.web.FasterFilterQuery` API.

## [1.0.3] - 2026-10-15

### Added

- Added `@FasterFilter` for validated dynamic filters in custom controller endpoints.
- Added `FasterFilters<T>` to expose raw filters and the generated JPA `Specification<T>`.
- Added `FasterFilterArgumentResolver` and `FasterFilterAutoConfiguration` for automatic filter resolution in Spring MVC.
- Added `SpecificationBuilder` for building validated JPA specifications.
- Added support for the `eq`, `like`, `gt`, `lt`, `gte`, and `lte` operators.
- Added configurable field whitelists and validation for unknown fields, unsupported operators, and incompatible field types.
- Added `FilterValidationException` and `FilterExceptionHandler` for descriptive HTTP 400 responses.

### Tests

- Added integration coverage for dynamic filter argument resolution, supported operators, and allowed-field validation.

### Documentation

- Added documentation and examples for `@FasterFilter`, dynamic operators, and filter validation.
- Added Portuguese versions of the security and contribution documentation.

## [1.0.2] - 2026-10-12

### Added

- Added support for configuring multiple model and DTO packages through `fasterapi.base-packages`, `fasterapi.model-packages`, and `fasterapi.dto-packages`.
- Added custom filter operators `eq`, `like`, `gt`, `lt`, `gte`, and `lte`, including numeric type validation.
- Added Spring Boot configuration metadata for package scanning properties.

### Changed

- Improved classpath scanning to combine configured packages and ignore duplicates.
- Added validation for empty package configurations and duplicated CRUD paths.
- Updated the release workflow to prepare versioned pull requests and publish releases after merge.
- Pinned the Maven Wrapper to Maven 3.9.16.

### Fixed

- Improved validation and error handling for package scanning and custom filters.

### Documentation

- Updated the README with Maven Central installation, package scanning, and configuration guidance.

## [1.0.1] - 2026-10-09

### Added

- Published the FasterAPI starter to Maven Central as `io.github.rokaideveloper:fasterapi-spring-boot-starter:1.0.1`.
- Added bilingual project documentation and Portuguese security and contribution policies.

### Changed

- Changed the published Java baseline from Java 21 to Java 17.
- Confirmed compatibility with Java 17 and Java 21.
- Updated the documentation with Maven Central installation and Java compatibility details.

## [1.0.0] - 2026-10-09

### Added

- Published the first official FasterAPI starter release to Maven Central.
- Added dynamic CRUD generation with selective GET, POST, PUT, PATCH, and DELETE operations.
- Added automatic OpenAPI documentation for generated CRUD endpoints.
- Added Spring Boot auto-configuration for the starter.
- Added automatic and manual DTO support, pagination, sorting, filters, PATCH, `@ReadOnly`, `@EntityMapping`, and `@JsonIgnore` handling.

[1.0.5]: https://github.com/RokaiDeveloper/FasterAPI/releases/tag/v1.0.5
[1.0.4]: https://github.com/RokaiDeveloper/FasterAPI/releases/tag/v1.0.4
[1.0.3]: https://github.com/RokaiDeveloper/FasterAPI/releases/tag/v1.0.3
[1.0.2]: https://github.com/RokaiDeveloper/FasterAPI/releases/tag/v1.0.2
[1.0.1]: https://github.com/RokaiDeveloper/FasterAPI/releases/tag/v1.0.1
[1.0.0]: https://github.com/RokaiDeveloper/FasterAPI/releases/tag/v1.0.0

---

## Português (Brasil)

Todas as mudanças relevantes do FasterAPI estão documentadas neste arquivo.

O formato segue o [Keep a Changelog](https://keepachangelog.com/pt-BR/1.1.0/),
e o projeto segue o [Versionamento Semântico](https://semver.org/lang/pt-BR/).

**English:** [View the English version](#changelog)

## [1.0.5] - 2026-10-16

### Corrigido

- Corrigido o registro automático de `FasterFilterAutoConfiguration` por meio dos imports de auto-configuração do Spring Boot.
- Corrigida a resolução de argumentos de `FasterFilterQuery<T>` em controllers personalizados.
- Corrigida a inicialização do repositório nas consultas de `FasterFilterQuery<T>`, evitando erros `NullPointerException` ao executar `findAll()` e `findAll(Pageable)`.

### Testes

- Adicionada cobertura de regressão para o registro automático da configuração de filtros e a inicialização do repositório em consultas de controllers personalizados.

## [1.0.4] - 2026-10-15

### Adicionado

- Adicionada `FasterFilterQuery<T>` como fachada de alto nível para controllers personalizados, ocultando `FasterFilters`, `Specification` do JPA e detalhes da implementação do repositório.
- Adicionado suporte à execução direta dos resultados filtrados com `findAll()` e `findAll(Pageable)`.

### Corrigido

- Corrigida a construção dos predicates numéricos para os operadores `gt`, `lt`, `gte` e `lte`.
- Corrigida a validação estrita de campos e operadores não permitidos, tipos incompatíveis e valores numéricos inválidos.
- Mantidos `page`, `size` e `sort` como parâmetros ignorados pelo resolver.

### Testes

- Adicionada cobertura de integração para todos os operadores comparativos, valores inválidos, validação estrita, parâmetros de paginação ignorados e a API simplificada `FasterFilterQuery<T>`.

### Documentação

- Adicionados exemplos de controllers personalizados usando a API simplificada `com.rokaidev.fasterapi.web.FasterFilterQuery`.

## [1.0.3] - 2026-10-15

### Adicionado

- Adicionada a anotação `@FasterFilter` para filtros dinâmicos validados em endpoints de controllers personalizados.
- Adicionada a classe `FasterFilters<T>` para expor os filtros brutos e a `Specification<T>` gerada para o JPA.
- Adicionados `FasterFilterArgumentResolver` e `FasterFilterAutoConfiguration` para resolução automática de filtros no Spring MVC.
- Adicionado o `SpecificationBuilder` para construção de specifications JPA validadas.
- Adicionado suporte aos operadores `eq`, `like`, `gt`, `lt`, `gte` e `lte`.
- Adicionadas listas configuráveis de campos permitidos e validações para campos desconhecidos, operadores não suportados e tipos incompatíveis.
- Adicionados `FilterValidationException` e `FilterExceptionHandler` para respostas HTTP 400 descritivas.

### Testes

- Adicionados testes de integração para resolução de argumentos, operadores suportados e validação de campos permitidos em filtros dinâmicos.

### Documentação

- Adicionada documentação e exemplos para `@FasterFilter`, operadores dinâmicos e validação de filtros.
- Adicionadas versões em português da documentação de segurança e contribuição.

## [1.0.2] - 2026-10-12

### Adicionado

- Adicionado suporte à configuração de múltiplos pacotes de modelos e DTOs através de `fasterapi.base-packages`, `fasterapi.model-packages` e `fasterapi.dto-packages`.
- Adicionados operadores de filtro personalizados `eq`, `like`, `gt`, `lt`, `gte` e `lte`, incluindo validação de tipos numéricos.
- Adicionados metadados de configuração do Spring Boot para as propriedades de varredura de pacotes.

### Alterado

- Melhorada a varredura do classpath para combinar pacotes configurados e ignorar duplicidades.
- Adicionadas validações para configurações de pacotes vazias e paths CRUD duplicados.
- Atualizado o workflow de release para preparar pull requests versionados e publicar releases após o merge.
- Fixada a versão do Maven Wrapper em 3.9.16.

### Corrigido

- Melhorados a validação e o tratamento de erros na varredura de pacotes e nos filtros personalizados.

### Documentação

- Atualizado o README com instruções de instalação via Maven Central e orientações de configuração e varredura de pacotes.

## [1.0.1] - 2026-10-09

### Adicionado

- Publicado o starter FasterAPI no Maven Central como `io.github.rokaideveloper:fasterapi-spring-boot-starter:1.0.1`.
- Adicionada documentação bilíngue e políticas de segurança e contribuição em português.

### Alterado

- Alterada a versão base publicada do Java de 21 para 17.
- Confirmada a compatibilidade com Java 17 e Java 21.
- Atualizada a documentação com instruções de instalação via Maven Central e detalhes de compatibilidade do Java.

## [1.0.0] - 2026-10-09

### Adicionado

- Publicado o primeiro release oficial do starter FasterAPI no Maven Central.
- Adicionada a geração dinâmica de CRUD com operações GET, POST, PUT, PATCH e DELETE selecionáveis.
- Adicionada documentação automática OpenAPI para endpoints CRUD gerados.
- Adicionada auto-configuração Spring Boot para o starter.
- Adicionado suporte a DTOs automáticos e manuais, paginação, ordenação, filtros, PATCH, `@ReadOnly`, `@EntityMapping` e tratamento de `@JsonIgnore`.
