package com.digitalsubstrate.converter;

/**
 * How the generated static surface reaches the runtime, which decides whether a
 * container the model never declares still has to be generated.
 *
 * <p>A NATIVE binding holds native typed data and crosses to the runtime through
 * an explicit codec, so it builds a container element by element — an attachment
 * {@code keys()} iterates the runtime set and decodes each key, {@code get()}
 * tests the blob then decodes the document. It needs no generated functions for
 * the set or the optional themselves.
 *
 * <p>A DELEGATING binding has no native representation to hold: the static object
 * is a proxy wrapping one runtime value. An attachment {@code keys()} therefore
 * returns a generated {@code Set_<Key>} proxy and {@code get()} a generated
 * {@code Optional_<Document>} one, neither of which the model declares. Those
 * container types are derived from the attachment and must be registered for the
 * proxies to exist.
 */
public enum Binding {
    NATIVE,
    DELEGATING;

    boolean needsDerivedContainerProxies() {
        return this == DELEGATING;
    }
}
