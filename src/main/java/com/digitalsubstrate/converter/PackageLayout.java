package com.digitalsubstrate.converter;

/**
 * One package directory, and file names that carry nothing.
 *
 * <p>A delegating target's output is a package — a Python one, an npm one — whose
 * directory already is the scope, so a file repeats nothing: {@code data.py},
 * {@code value_type.py}. The template file's own name is the answer.
 *
 * <p>{@link #artefactPath} is deliberately unimplemented. These targets reach another
 * artefact by importing a module, not by naming a path, and no first-party template
 * asks yet. Answering with a plausible guess would put an invented convention into
 * generated code and make it look decided; failing says what is actually true.
 */
public final class PackageLayout implements TargetLayout {

    private final String target;

    public PackageLayout(String target) {
        this.target = target;
    }

    @Override
    public String outputFileName(String unit, String templateBaseName) {
        return templateBaseName;
    }

    @Override
    public String artefactPath(String unit, String artefact) {
        throw new UnsupportedOperationException(
            "the " + target + " target has no artefact path yet: '" + artefact + "' was asked for. "
          + "A delegating target reaches another artefact by importing a module, and that "
          + "convention is not settled.");
    }
}
