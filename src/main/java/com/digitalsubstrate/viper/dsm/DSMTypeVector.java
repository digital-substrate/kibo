package com.digitalsubstrate.viper.dsm;

import com.digitalsubstrate.viper.NameSpace;

public final class DSMTypeVector extends DSMType {

    public final DSMType elementType;

    public DSMTypeVector(DSMType elementType) {
        this.elementType = elementType;
    }

    @Override
    public String representation() {
        return DSMLexicon.Vector + "<" + elementType.representation() + ">";
    }

    @Override
    public String representationIn(NameSpace nameSpace) {
        return DSMLexicon.Vector + "<" + elementType.representationIn(nameSpace) + ">";
    }
}
