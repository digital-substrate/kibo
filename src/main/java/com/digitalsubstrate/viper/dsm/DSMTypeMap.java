package com.digitalsubstrate.viper.dsm;

import com.digitalsubstrate.viper.NameSpace;

public final class DSMTypeMap extends DSMType {

    public final DSMType keyType;
    public final DSMType elementType;

    public DSMTypeMap(DSMType keyType, DSMType elementType) {
        this.keyType = keyType;
        this.elementType = elementType;
    }

    @Override
    public String representation() {
        return DSMLexicon.Map + "<" + keyType.representation() + ", " + elementType.representation() + ">";
    }

    @Override
    public String representationIn(NameSpace nameSpace) {
        return DSMLexicon.Map + "<" + keyType.representationIn(nameSpace) + ", " + elementType.representationIn(nameSpace) + ">";
    }
}
