// Copyright (c) Digital Substrate 2026, All rights reserved.

package com.digitalsubstrate.viper.dsm;

import com.digitalsubstrate.viper.NameSpace;

public final class DSMTypeKey extends DSMType {

    public final DSMTypeReference elementType;

    public DSMTypeKey(DSMTypeReference type) {
        this.elementType = type;
    }

    @Override
    public String representation() {
        return DSMLexicon.Key + "<" + elementType.representation() + ">";
    }

    @Override
    public String representationIn(NameSpace nameSpace) {
        return DSMLexicon.Key + "<" + elementType.representationIn(nameSpace) + ">";
    }
}
