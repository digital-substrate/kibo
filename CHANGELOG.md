# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

Kibo carries its own version line (declared in `pom.xml`), independent from
the DSM language contract it consumes and from any runtime targeted by the
templates it renders.

## [1.2.10] - 2026-06-17

Maintenance baseline. This changelog begins at the current released
version; earlier changes predate it and are not itemised here.

Kibo is the consumer-side implementation of the DSM language contract: it
reads the canonical `dsm-json` wire format, exposes a Template Model to
StringTemplate `.stg` files, and renders the output. The generated target
(C++, Python, …) is determined entirely by the templates, not by Kibo.
