package com.digitalsubstrate.converter;

import com.digitalsubstrate.template.TemplateTool;

/**
 * A package, one sub-package per unit, and file names that carry nothing.
 *
 * <p>A delegating target's output is a package — a Python one, an npm one — and there a
 * unit is not a prefix on a file name but a <em>directory</em>. {@code ModelA::Colour} is
 * {@code topology.modela.Colour} and {@code ModelB::Colour} is
 * {@code topology.modelb.Colour}: the two coexist without a name moving, which is the whole
 * point, and it costs a directory rather than a renaming rule.
 *
 * <p>So the unit decides the directory and the template file decides the file:
 * {@code Data.py.stg} rendered for {@code ModelA} is {@code modela/data.py}. Nothing is
 * inferred from the template's name beyond stripping its extension — a layout that mapped
 * {@code Data} to {@code __init__.py} would be a convention hidden in the generator, and a
 * pack could not opt out of it. A pack that wants a package initialiser writes a template
 * called {@code __init__.py.stg}.
 *
 * <p>What the model itself carries — what no unit can claim — sits at the package root.
 */
public final class PackageLayout implements TargetLayout {

    private final String target;

    public PackageLayout(String target) {
        this.target = target;
    }

    @Override
    public String outputFileName(Scope scope, String unit, String templateBaseName) {
        return scope == Scope.MODEL
            ? templateBaseName
            : TemplateTool.lsc(unit) + "/" + templateBaseName;
    }

    /**
     * The module, named from the package root — and the leading dots are the template's.
     *
     * <p>A relative import's depth depends on where the <em>importing</em> file sits, and
     * that is the one thing this cannot know: it is asked by the artefact being reached,
     * not by the one doing the reaching. A template does know its own depth, because it is
     * written either for a unit or for the model and never for both. So the answer stops at
     * the root — {@code modelb.data} — and the template writes {@code from ..<path> import}
     * or {@code from .<path> import} according to what it is.
     *
     * <p>Relative rather than absolute because the package's own name is the project's
     * choice, not the model's: the same model is published as {@code topology} by one build
     * and vendored under another name by the next, and no emitted byte should have to
     * change for that.
     */
    @Override
    public String artefactPath(Scope scope, String unit, String artefact) {
        final var module = TemplateTool.lsc(String.valueOf(artefact));
        return scope == Scope.MODEL ? module : TemplateTool.lsc(unit) + "." + module;
    }
}
