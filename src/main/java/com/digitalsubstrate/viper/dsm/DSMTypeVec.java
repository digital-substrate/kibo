// Copyright (c) Digital Substrate 2026, All rights reserved.

package com.digitalsubstrate.viper.dsm;

import com.digitalsubstrate.viper.NameSpace;

public final class DSMTypeVec extends DSMType {

    public final DSMTypeReference elementType;
    public final long size;

    public DSMTypeVec(DSMTypeReference elementType, long size) {
        this.elementType = elementType;
        this.size = size;
    }

    @Override
    public String representation() {
        return DSMLexicon.Vec + "<" + elementType.representation() + ", " + size + ">";
    }

    @Override
    public String representationIn(NameSpace nameSpace) {
        return DSMLexicon.Vec + "<" + elementType.representationIn(nameSpace) + ", " + size + ">";
    }
}
