# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

Kibo carries its own version line (declared in `pom.xml`), independent from
the DSM language contract it consumes and from any runtime targeted by the
templates it renders.

## [Unreleased]

A TypeScript surface fix, template render diagnostics, and build tooling. The DSM
language and the Template Model are unchanged; the generated surfaces change only
where a function returns `void`.

### Added

- **A render reports the expressions it could not resolve.** A template that reads
  an accessor the Template Model does not carry rendered the empty string: the
  render succeeded, the output was silently short, and nothing reached stderr. A
  template written against an older model therefore kept producing files, minus the
  parts that no longer resolved — and a model change was impossible to migrate
  against, because nothing said what had stopped working.

  Every render now installs a listener and prints each distinct diagnostic once,
  with how many times it fired, naming the template and the expression. They are
  warnings: the file is still written and kibo still exits zero, since whether the
  output is acceptable is the operator's call. Clean renders print nothing.

  Turned on across the codegen test fixtures and two application models, it reported
  five distinct diagnostics on the first run, two of them defects in the first-party
  templates: an xarray `remove` function registered under a truncated prototype name,
  and a conditional include guarded by an accessor the model does not carry. Both
  live in `kibo-template-viper` and are tracked there.

- **The repository carries the Maven Wrapper.** Building kibo required a Maven
  on the machine, which in practice meant the one bundled inside IntelliJ. The
  wrapper pins Maven in `.mvn/wrapper/maven-wrapper.properties` and downloads it
  on first use, so a JDK 17 is now the only prerequisite: `./mvnw clean package`
  (`.\mvnw.cmd` on Windows). No IDE is involved, and every machine builds with
  the same Maven.

### Changed

- **The executable jar is now assembled by the shade plugin.** The jar plugin
  and the assembly plugin both wrote to `target/kibo-X.Y.Z.jar`, so the assembly
  overwrote the main artifact and Maven warned on every build. Shade replaces
  the main artifact in place, which is what the single-jar deliverable was
  always meant to be. The output path and name are unchanged.

  The rebuilt manifest also declares `Multi-Release: true`. The jar has always
  carried Jackson's per-release class overrides under `META-INF/versions/`, but
  without that attribute the runtime ignored them. Dependency module descriptors
  are now excluded, and the dependencies' `NOTICE` and `LICENSE` files are
  concatenated rather than one silently winning; the dependencies' own manifests
  are dropped, since the jar's manifest is written from the POM. Nothing is left
  for the build to report as an overlapping resource, so `package` is now silent.

### Fixed

- **A `void` return rendered as a proxy class in TypeScript.** `void` is a
  registered DSM primitive, but it was missing from the passthrough list that
  `TemplatePythonType.getUseProxy()` answers, so the predicate reported `true`
  for it. A target that resolves a primitive spelling by testing that predicate
  and then looking the DSM name up in its own table never reached its `void`
  entry: the predicate sent it down the proxy branch, where the DSM name is
  emitted as though it were a generated class. The TypeScript templates render
  a function pool's void function as `reset(): d.void`, which does not compile.

  `void` now answers `false`, so the table is consulted and the entry it already
  carried is used. The Python surface is unaffected — it resolves primitive
  spellings through a separate accessor whose mapping has always had a `void`
  arm.

  No test model declares a function pool with a `void` return, which is why the
  generated output never showed this.

## [1.2.11] - 2026-08-28

Anticipated container types become a per-target decision, and a CLI that could
not print its own version. The DSM language, the Template Model and the Python
and TypeScript surfaces are unchanged.

### Changed

- **Anticipated container types are now registered per binding style.** Some
  containers must be generated although the model never declares them: an
  attachment's `keys()` returns a set of its key type and its `get()` an
  optional of its document type, and an xarray proxy exposes a vector of its
  element type. Kibo registered those for every target, plus an `optional` per
  concept, per club and of `AnyConcept`.

  Which of them a target actually needs depends on how its generated surface
  reaches the runtime. A **native** binding holds typed data and crosses through
  a codec, so it builds those containers element by element — `keys()` iterates
  the runtime set and decodes each key — and names no function for the container
  itself. A **delegating** binding has no native representation: the static
  object is a proxy over one runtime value, so `keys()` returns a generated
  `Set_<Key>` class and `get()` an `Optional_<Document>` one, neither of which
  the model declares.

  The C++ arm is native and now registers only what the model declares. The
  Python and TypeScript arms are delegating and are unchanged. On a model with
  151 concepts and 366 attachments, the C++ surface falls from 392 registered
  `optional` types to 24 and from 287571 to 252191 lines; the Python surface
  keeps every proxy class it had.

  A new `Binding` enum carries the distinction, set by `generateCpp` and
  `generatePython` — TypeScript inherits it by reusing the latter.

### Fixed

- **`--help` and `--version` crashed instead of printing.** The banner string
  handed to the templates — `Generated from <definitions> by kibo-X.Y.Z.jar` —
  was built before the two early exits, dereferencing the definitions path.
  Both options are declared `help = true`, so JCommander skips the
  required-option check for them and the path is legitimately null; every
  invocation of either ended on a `NullPointerException`. The banner is now
  built after the early exits, where it is first needed.

- **The `optional` of a club key was registered once per club member instead of
  once per club.** The call sat inside the loop over `club.members` although it
  did not depend on the member, so a club with no member registered nothing at
  all while its generated `from(AnyConceptKey)` still returned a
  `std::optional<ClubKey>` — a type with no codec and no way back from the
  dynamic side. Hoisting the call out of the loop is not a neutral refactor
  precisely because the loop can run zero times. Superseded by the change above,
  which drops the registration entirely.

## [1.2.10] - 2026-06-17

Maintenance baseline. This changelog begins at the current released
version; earlier changes predate it and are not itemised here.

Kibo is the consumer-side implementation of the DSM language contract: it
reads the canonical `dsm-json` wire format, exposes a Template Model to
StringTemplate `.stg` files, and renders the output. The generated target
(C++, Python, …) is determined entirely by the templates, not by Kibo.
