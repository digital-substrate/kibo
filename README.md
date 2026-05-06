# kibo

Code generator for the dsviper ecosystem. Kibo is a thin bridge between
[DSM](https://github.com/digital-substrate/dsm) and
[StringTemplate](https://www.stringtemplate.org/): it reads a DSM
definitions file, exposes a Template Model to `.stg` files, and lets
StringTemplate render the output.

```
Kibo = bridge(.dsm.json | .dsmb)  →  StringTemplate model  →  generated code
```

Kibo accepts the DSM definitions in two formats: the canonical JSON
wire format specified by the [dsm](https://github.com/digital-substrate/dsm)
language repo, and the legacy `.dsmb` binary format. Dispatch is by
file extension on `-d`.

Kibo does not know what is being generated. It only applies a
templated feature (a `.stg` file) to a model. Targets can be anything
StringTemplate can render: C++, Python, Graphviz, SQL, …

## Build

```bash
mvn package
```

Produces `target/kibo-X.Y.Z.jar`.

## Synopsis

```bash
java -jar target/kibo-X.Y.Z.jar \
    -c [cpp | python] \
    -n [namespace] \
    -d [definitions.dsm.json | definitions.dsmb] \
    -t [template_directory_or_file] \
    -o [output_directory]
```

For details, see the user-facing documentation:

- `devkit-doc/source/tools/kibo.md` — Kibo CLI and its role.
- `devkit-doc/source/tools/templates.md` — templated features as an
  ecosystem.
- `devkit-doc/source/tools/template_model.md` — how the Template Model
  is built from `.dsmb`.

## Public contract

Kibo's public surface is the Template Model API exposed to `.stg`
files: the variables, iterators, and methods consumed by every
templated feature, first-party (DS-maintained) and third-party
(client-authored). Breaking this API breaks all downstream templated
features, including those DS does not own. Treat changes with the
same rigour as a public runtime API.

Versioning follows the dsviper ecosystem MAJOR.MINOR API line; see
[https://docs.digitalsubstrate.io/](https://docs.digitalsubstrate.io/).

## License

This project is licensed under the MIT License — see [LICENSE](LICENSE).
