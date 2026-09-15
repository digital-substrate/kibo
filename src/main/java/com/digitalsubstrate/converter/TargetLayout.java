package com.digitalsubstrate.converter;

/**
 * Where a target's generated artefacts go, and how one reaches another.
 *
 * <p>Two questions, and they are the same question asked from the two ends: the writer
 * asks what to call the file it is about to save, the reader asks what path to write in
 * order to reach it. A layout that answered them separately could answer them
 * inconsistently, which is a class of defect no template could catch — it would render
 * perfectly and fail at compile time.
 *
 * <p>This is the only thing that forks per target. What kibo computes about a model is
 * the same for all of them; where the result lands is not, because a C++ namespace is
 * free of file layout while a Python module <em>is</em> a location.
 *
 * <p>Which is why the generator owns the layout rather than a project's build script. For
 * a target whose modules are directories, a file's path and its own contents have to agree:
 * a module that says {@code from ..modelb import Colour} is correct in one place and wrong
 * in every other. A generator that emitted flat files for a script to move afterwards would
 * be emitting text that is wrong where it is written, and right only once something else
 * has run.
 */
public interface TargetLayout {

    /**
     * What a render is for — the whole model, or one unit of it.
     *
     * <p>A pool is a unit: it holds only functions, and its name is already a scope. The
     * layout has no reason to tell the two apart, and one fewer case is one fewer place
     * for them to be treated differently by accident.
     */
    enum Scope { MODEL, UNIT }

    /** What a render is saved as, relative to {@code -o}, for a template file's base name. */
    String outputFileName(Scope scope, String unit, String templateBaseName);

    /** What a template writes to reach another artefact of this unit. */
    String artefactPath(Scope scope, String unit, String artefact);
}
