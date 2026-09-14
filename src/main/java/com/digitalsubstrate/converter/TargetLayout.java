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
 */
public interface TargetLayout {

    /** What a render is saved as, relative to {@code -o}, for a template file's base name. */
    String outputFileName(String unit, String templateBaseName);

    /** What a template writes to reach another artefact of this unit. */
    String artefactPath(String unit, String artefact);
}
