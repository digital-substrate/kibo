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

    public String representationIn(NameSpace nameSpace) {
        return nameSpace.representationIn(nameSpace, name);
    }
}

