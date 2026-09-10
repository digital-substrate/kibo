# kibo

Code generator for the DSM language ecosystem. Kibo is a thin bridge between
DSM and [StringTemplate](https://www.stringtemplate.org/): it reads a
DSM definitions file, exposes a Template Model to `.stg` files, and
lets StringTemplate render the output.

```
Kibo = bridge(.dsm.json)  →  StringTemplate model  →  generated code
```

Kibo is an **implementation of the consumer side** of the DSM language
contract specified at
[github.com/digital-substrate/dsm](https://github.com/digital-substrate/dsm).
It accepts the canonical JSON wire format described by that spec.

Kibo does not know what is being generated. It only applies a
templated feature (a `.stg` file) to a model. Targets can be anything
StringTemplate can render: C++, Python, Graphviz, SQL, …

## Documentation

Full documentation: https://docs.digitalsubstrate.io/kibo/

Part of the [DevKit ecosystem](https://docs.digitalsubstrate.io/).

## Build

Requires a JDK 17 (a JRE is not enough). Maven itself is not a prerequisite:
the repository carries the Maven Wrapper, which downloads and caches the
pinned Maven version on first use. No IDE is involved.

```bash
./mvnw clean package     # macOS, Linux
.\mvnw.cmd clean package # Windows
```

Produces `target/kibo-X.Y.Z.jar`, an executable jar bundling its dependencies.
That is the artifact to ship; `target/original-kibo-X.Y.Z.jar` alongside it is
the pre-bundling jar kept by the shade plugin and carries no dependencies.

The Maven version is pinned in `.mvn/wrapper/maven-wrapper.properties`; change
it there rather than relying on whatever `mvn` a given machine happens to have.

## Synopsis

```bash
java -jar target/kibo-X.Y.Z.jar \
    -c [cpp | python] \
    -n [namespace] \
    -d [definitions.dsm.json] \
    -t [template_directory_or_file] \
    -o [output_directory]
```

For details, see the user-facing documentation:

- `devkit-doc/source/kibo/usage.md` — Kibo CLI and its role.
- `devkit-doc/source/kibo/templates.md` — templated features as an
  ecosystem.
- `devkit-doc/source/kibo/template_model.md` — how the Template Model
  is built from `.dsm.json`.

## Public contract

Kibo's public surface is the Template Model API exposed to `.stg`
files: the variables, iterators, and methods consumed by every
templated feature, first-party (DS-maintained) and third-party
(client-authored). Breaking this API breaks all downstream templated
features, including those DS does not own. Treat changes with the
same rigour as a public runtime API.

kibo's version is its own. It is a product, and its number says nothing
about the two contracts it sits between; those are declared separately,
and each moves on its own cadence:

| | |
|---|---|
| **input** | DSM 1.0, in the format specified by the [`dsm`](https://github.com/digital-substrate/dsm) repository |
| **output** | Template Model 2, specified in this repository's documentation |

A Template Model revision is not a kibo minor, and a kibo minor does not
imply one. The CHANGELOG says which release moved which. Moving a
template pack across a Template Model revision is covered in
[MIGRATING.md](MIGRATING.md).

## License

This project is licensed under the MIT License — see [LICENSE](LICENSE).
