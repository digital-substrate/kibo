package com.digitalsubstrate.viper.dsm;

import com.digitalsubstrate.viper.NameSpace;

import java.util.ArrayList;

public final class DSMTypeVariant extends DSMType {

    public final ArrayList<DSMType> types;

    public DSMTypeVariant(ArrayList<DSMType> types) {
        this.types = types;
    }

    @Override
    public String representation() {
        final var typeRepresentations = new ArrayList<String>();
        for (var type : types)
            typeRepresentations.add(type.representation());

        return DSMLexicon.Variant + "<" + String.join(", ", typeRepresentations) + ">";
    }

    @Override
    public String representationIn(NameSpace nameSpace) {
        final var typeRepresentations = new ArrayList<String>();
        for (var type : types)
            typeRepresentations.add(type.representationIn(nameSpace));

        return DSMLexicon.Variant + "<" + String.join(", ", typeRepresentations) + ">";
    }
}


