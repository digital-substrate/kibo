// Copyright (c) Digital Substrate 2026, All rights reserved.

package com.digitalsubstrate.viper.dsm;

import com.digitalsubstrate.viper.NameSpace;

public final class DSMTypeXArray extends DSMType {

    public final DSMType elementType;

    public DSMTypeXArray(DSMType elementType) {
        this.elementType = elementType;
    }

    @Override
    public String representation() {
        return DSMLexicon.XArray + "<" + elementType.representation() + ">";
    }

    @Override
    public String representationIn(NameSpace nameSpace) {
        return DSMLexicon.XArray + "<" + elementType.representationIn(nameSpace) + ">";
    }
}
