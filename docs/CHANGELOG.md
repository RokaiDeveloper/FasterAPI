# Changelog

All notable changes to FasterAPI are documented in this file.

## [1.0.3] - 2026-10-15

### Added

- Added `@FasterFilter` for enabling validated dynamic filters in custom
  controller endpoints.
- Added `FasterFilters<T>` to expose the raw filters and the generated JPA
  `Specification<T>`.
- Added support for the `eq`, `like`, `gt`, `lt`, `gte`, and `lte` filter
  operators.
- Added configurable field whitelists and strict or lenient validation modes.
- Added validation for unknown fields, unsupported operators, and incompatible
  field types.
- Added HTTP 400 error handling with descriptive filter validation messages.
- Added integration coverage for custom filter argument resolution and
  validation behavior.

## [1.0.2] - 2026-10-09

### Added

- Added support for configuring multiple model and DTO packages through
  `fasterapi.base-packages`, `fasterapi.model-packages`, and
  `fasterapi.dto-packages`.
- Added custom filter operators `eq`, `like`, `gt`, `lt`, `gte`, and `lte`,
  including numeric type validation.
- Added Spring Boot configuration metadata for the package scanning
  properties.

### Changed

- Improved classpath scanning to combine configured packages and ignore
  duplicates.
- Added validation for empty package configurations and duplicated CRUD paths.
- Updated the release workflow to prepare versioned pull requests and publish
  releases after merge.
- Pinned the Maven Wrapper to Maven 3.9.16.

### Fixed

- Improved validation and error handling for package scanning and custom
  filters.

## [1.0.1] - 2026-10-09

### Changed

- Changed the published Java baseline from Java 21 to Java 17.
- Confirmed compatibility with Java 17 and Java 21.
- Published the starter to Maven Central as
  `io.github.rokaideveloper:fasterapi-spring-boot-starter:1.0.1`.
- Updated the documentation with Maven Central installation and Java
  compatibility details.
- Added bilingual project documentation and GitHub contribution and security
  policies.

## [1.0.0] - 2026-10-09

### Added

- Published the first official FasterAPI starter release to Maven Central.
- Added dynamic CRUD generation with selective GET, POST, PUT, PATCH, and
  DELETE operations.
- Added automatic OpenAPI documentation for generated CRUD endpoints.
- Added Spring Boot auto-configuration for the starter.
- Added automatic and manual DTO support, pagination, sorting, filters, PATCH,
  `@ReadOnly`, `@EntityMapping`, and `@JsonIgnore` handling.

[1.0.2]: https://github.com/RokaiDeveloper/FasterAPI/releases/tag/v1.0.2
[1.0.3]: https://github.com/RokaiDeveloper/FasterAPI/releases/tag/v1.0.3
[1.0.1]: https://github.com/RokaiDeveloper/FasterAPI/releases/tag/v1.0.1
[1.0.0]: https://github.com/RokaiDeveloper/FasterAPI/releases/tag/v1.0.0
