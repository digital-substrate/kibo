// Copyright (c) Digital Substrate 2026, All rights reserved.

package com.digitalsubstrate.viper.dsm;

import com.digitalsubstrate.viper.NameSpace;

public final class DSMTypeOptional extends DSMType {

    public final DSMType elementType;

    public DSMTypeOptional(DSMType type) {
        this.elementType = type;
    }

    @Override
    public String representation() {
        return DSMLexicon.Optional + "<" + elementType.representation() + ">";
    }

    @Override
    public String representationIn(NameSpace nameSpace) {
        return DSMLexicon.Optional + "<" + elementType.representationIn(nameSpace) + ">";
    }
}
