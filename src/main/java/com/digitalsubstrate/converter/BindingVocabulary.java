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
}
