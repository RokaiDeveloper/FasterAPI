# Contributing to FasterAPI

Thank you for reporting problems and suggesting improvements.

---

**[Português (Brasil)](#português-brasil)**

## Before contributing

Open an Issue before starting a substantial change. Use Discussions for
questions, usage support, and early design ideas. Do not disclose security
vulnerabilities publicly; follow [SECURITY.md](SECURITY.md).

Before proposing code, confirm that:

- the change is compatible with Java 17, unless a compatibility change is
  explicitly approved;
- tests cover the changed behavior;
- public API and behavior changes are documented;
- no credentials, private keys, personal data, or generated build output are
  included;
- all submitted code is original or compatible with this repository's license.

## Reporting bugs

Use the bug report template when available. Include:

- FasterAPI version;
- Java and Spring Boot versions;
- database and relevant dependency versions;
- minimal reproduction;
- expected and actual behavior;
- relevant logs or stack traces with secrets removed.

## Pull Requests

Pull Requests are accepted only at the maintainer's discretion and do not
create a right to modify, fork, or redistribute FasterAPI. Keep each Pull
Request focused and explain the motivation, compatibility impact, and
validation performed.

Before submitting:

```bash
./mvnw clean verify
```

When the change affects the supported runtime, also validate the relevant Java
versions. The current minimum supported runtime is Java 17.

Pull Requests should:

- include or update tests;
- preserve existing behavior unless the change is intentional;
- update README or release notes when public behavior changes;
- avoid unrelated formatting or dependency changes;
- avoid changing the license or copyright notices.

The maintainer may request revisions, reject a change, or incorporate the idea
using a different implementation.

## Contribution rights

The FasterAPI source code is proprietary. The license permits use of the
unmodified published binary as a dependency, but does not generally permit
copying, modifying, forking, or creating derivative versions.

By submitting code, documentation, or other material to this repository, you
represent that you have the right to submit it and grant RokaiDeveloper and
the copyright holder a worldwide, perpetual, irrevocable, royalty-free,
non-exclusive, sublicensable license to use, reproduce, modify, incorporate,
publish, distribute, and relicense the contribution as part of FasterAPI.
The contribution may be distributed under the project's proprietary license
or another license selected by the copyright holder.

The submission of a Pull Request does not transfer copyright ownership unless
a separate written agreement says otherwise. The maintainer may require a
Contributor License Agreement or other written permission before accepting
code contributions.

Do not submit code copied from FasterAPI into another project, code copied from
an incompatible license, or changes intended to create a competing derivative
framework.

## Review and merge

The `main` branch is protected. Changes should enter through a reviewed Pull
Request with passing CI checks. Maintainer approval is required before merge.

All contributions remain subject to the
[FasterAPI Proprietary Free-Use License](LICENSE).

---

# Português (Brasil)

Agradecemos por relatar problemas e sugerir melhorias.

## Antes de contribuir

Abra uma issue antes de iniciar uma alteração substancial. Use as Discussions para perguntas, suporte de uso e ideias iniciais de design. Não divulgue vulnerabilidades de segurança; siga a [SECURITY.md](SECURITY.md).

Antes de propor código, confirme que:

- a alteração é compatível com Java 17, salvo aprovação explícita de uma alteração de compatibilidade;
- os testes cobrem o comportamento alterado;
- alterações na API pública e no comportamento estão documentadas;
- nenhuma credencial, chave privada, dado pessoal ou saída de build gerada foi incluída;
- todo código enviado é original ou compatível com a licença deste repositório.

## Relatando bugs

Use o template de bug quando disponível. Inclua:

- versão do FasterAPI;
- versões do Java e do Spring Boot;
- banco de dados e versões das dependências relevantes;
- reprodução mínima;
- comportamento esperado e comportamento real;
- logs ou stack traces relevantes, com os segredos removidos.

## Pull Requests

Pull Requests são aceitos somente a critério do mantenedor e não criam direito de modificar, fazer fork ou redistribuir o FasterAPI. Mantenha cada Pull Request focado e explique a motivação, o impacto de compatibilidade e a validação realizada.

Antes de enviar:

```bash
./mvnw clean verify
```

Quando a alteração afetar o runtime suportado, valide também as versões relevantes do Java. O runtime mínimo suportado atualmente é o Java 17.

Pull Requests devem:

- incluir ou atualizar testes;
- preservar o comportamento existente, salvo quando a alteração for intencional;
- atualizar o README ou as notas de versão quando o comportamento público mudar;
- evitar alterações não relacionadas de formatação ou dependências;
- evitar alterar a licença ou os avisos de copyright.

O mantenedor pode solicitar revisões, rejeitar uma alteração ou incorporar a ideia usando uma implementação diferente.

## Direitos sobre contribuições

O código-fonte do FasterAPI é proprietário. A licença permite o uso do binário publicado sem modificações como dependência, mas em geral não permite copiar, modificar, fazer fork ou criar versões derivadas.

Ao enviar código, documentação ou outro material para este repositório, você declara ter o direito de enviá-lo e concede à RokaiDeveloper e ao titular dos direitos autorais uma licença mundial, perpétua, irrevogável, isenta de royalties, não exclusiva e sublicenciável para usar, reproduzir, modificar, incorporar, publicar, distribuir e relicenciar a contribuição como parte do FasterAPI. A contribuição pode ser distribuída sob a licença proprietária do projeto ou outra licença escolhida pelo titular dos direitos autorais.

O envio de um Pull Request não transfere a titularidade dos direitos autorais, salvo se um acordo escrito separado determinar o contrário. O mantenedor pode exigir um Contributor License Agreement ou outra autorização escrita antes de aceitar contribuições de código.

Não envie código copiado do FasterAPI para outro projeto, código copiado de uma licença incompatível ou alterações destinadas a criar um framework derivado concorrente.

## Revisão e merge

A branch `main` é protegida. As alterações devem entrar por meio de um Pull Request revisado com verificações de CI aprovadas. A aprovação do mantenedor é necessária antes do merge.

Todas as contribuições continuam sujeitas à [FasterAPI Proprietary Free-Use License](LICENSE).
