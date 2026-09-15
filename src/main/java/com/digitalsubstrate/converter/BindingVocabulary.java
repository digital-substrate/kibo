package com.digitalsubstrate.converter;

/**
 * How one binding spells the types that cross it.
 *
 * <p>A delegating target reaches the runtime through a binding, and the binding decides
 * which DSM primitives cross as values of the host language and how those are written:
 * {@code int64} arrives as {@code int} in Python and as {@code bigint} in TypeScript, for
 * the very same runtime {@code ValueInt64}. That mapping is a property of the binding and
 * not of the type, so each target states its own — here, once, rather than in every
 * template that needs it.
 *
 * <p>Everything else about the binding space is shared and stays in the converter: the
 * generated proxy names are identical across bindings, and so is the predicate deciding
 * whether a type needs a proxy at all.
 */
public interface BindingVocabulary {

    /**
     * How the target writes a DSM primitive that crosses as a host value.
     */
    String leaf(String dsmPrimitive);

    /**
     * How the target writes a fixed-size sequence of {@code element}. Composed with
     * itself for a matrix, which is a sequence of columns.
     */
    String sequence(String element, long count);

    // ── how the target writes a composite, for an annotation ──
    //
    // A BINDING THAT GENERATES A CLASS PER SHAPE DOES NOT NEED THESE: it writes
    // `Vector_Parts_Colour` and the class carries the meaning. A binding that does not —
    // because one generic view serves every element type — has to spell the shape, and a
    // spelling that stops at "some container" throws away exactly what a type checker was
    // there to catch. These say it in full, so `Sequence[Colour]` is what a reader and a
    // checker both see.

    /** A sequence of {@code element}: a vector, a set, a fixed-size vec. */
    String list(String element);

    /** A sequence of sequences. */
    String matrix(String element);

    /** A fixed heterogeneous sequence. */
    String tuple(java.util.List<String> members);

    /** {@code element}, or nothing. */
    String optional(String element);

    /** An association from {@code key} to {@code element}. */
    String map(String key, String element);

    /** A sequence whose places have stable identities — an xarray. */
    String ordered(String element);

    /** One of several types — a variant. */
    String union(java.util.List<String> members);

    /** Anything the model does not constrain. */
    String any();
}
