// Copyright (c) Digital Substrate 2026, All rights reserved.

package com.digitalsubstrate.viper.dsm;

import com.digitalsubstrate.viper.NameSpace;

import java.util.ArrayList;

public final class DSMTypeTuple extends DSMType {

    public final ArrayList<DSMType> types;

    public DSMTypeTuple(ArrayList<DSMType> types) {
        this.types = types;
    }

    @Override
    public String representation() {
        final var typeRepresentations = new ArrayList<String>();
        for (var type : types)
            typeRepresentations.add(type.representation());

        return DSMLexicon.Tuple + "<" + String.join(", ", typeRepresentations) + ">";
    }

    @Override
    public String representationIn(NameSpace nameSpace) {
        final var typeRepresentations = new ArrayList<String>();
        for (var type : types)
            typeRepresentations.add(type.representationIn(nameSpace));

        return DSMLexicon.Tuple + "<" + String.join(", ", typeRepresentations) + ">";
    }
}

