# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

Kibo carries its own version line (declared in `pom.xml`), independent from
the DSM language contract it consumes and from any runtime targeted by the
templates it renders.

## [Unreleased]

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
