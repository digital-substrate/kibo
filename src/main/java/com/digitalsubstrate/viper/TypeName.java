package com.digitalsubstrate.viper;

import java.util.Objects;

public class TypeName {
    public final String name;
    public final NameSpace nameSpace;

    public TypeName(String name) {
       this.name = name;
       this.nameSpace = NameSpace.GLOBAL;
    }

    public TypeName(NameSpace nameSpace, String name) {
        this.nameSpace = nameSpace;
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof TypeName typeName)) return false;
        return Objects.equals(name, typeName.name) && Objects.equals(nameSpace, typeName.nameSpace);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, nameSpace);
    }

    public String representation() {
        return nameSpace.representation(name);
    }

    /**
     * How this name is written when read from {@code context}: bare inside its own
     * namespace, qualified from anywhere else.
     *
     * <p>The parameter is named {@code context} and not {@code nameSpace} on purpose.
     * It used to shadow the field, so the call below ran on the argument instead of on
     * this name's own namespace, the two UUIDs compared were the same object's, and the
     * method returned the bare name every time. With one namespace in a model that
     * answer is always right, which is why it survived from the day the DSM gained
     * namespaces until a model with five of them existed.
     */
    public String representationIn(NameSpace context) {
        return nameSpace.representationIn(context, name);
    }
}

