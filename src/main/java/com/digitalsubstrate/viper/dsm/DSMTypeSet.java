// Copyright (c) Digital Substrate 2026, All rights reserved.

package com.digitalsubstrate.viper.dsm;

import com.digitalsubstrate.viper.NameSpace;

public final class DSMTypeSet extends DSMType {

    public final DSMType elementType;

    public DSMTypeSet(DSMType elementType) {
        this.elementType = elementType;
    }

    @Override
    public String representation() {
        return DSMLexicon.Set + "<" + elementType.representation() + ">";
    }

    @Override
    public String representationIn(NameSpace nameSpace) {
        return DSMLexicon.Set + "<" + elementType.representationIn(nameSpace) + ">";
    }
}
