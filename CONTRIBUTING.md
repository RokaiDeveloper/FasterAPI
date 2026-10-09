# Contributing to FasterAPI

[Português (Brasil)](CONTRIBUTING.pt-BR.md)

Thank you for reporting problems and suggesting improvements.

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
