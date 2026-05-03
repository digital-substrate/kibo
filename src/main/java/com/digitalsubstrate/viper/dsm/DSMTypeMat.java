// Copyright (c) Digital Substrate 2026, All rights reserved.

package com.digitalsubstrate.viper.dsm;

import com.digitalsubstrate.viper.NameSpace;

public final class DSMTypeMat extends DSMType {

    public final DSMTypeReference elementType;
    public final long columns;
    public final long rows;

    public DSMTypeMat(DSMTypeReference elementType, long columns, long rows) {
        this.elementType = elementType;
        this.columns = columns;
        this.rows = rows;
    }

    @Override
    public String representation() {
        return DSMLexicon.Mat + "<" + elementType.representation() + ", " + columns + ", " + rows + ">";
    }

    @Override
    public String representationIn(NameSpace nameSpace) {
        return DSMLexicon.Mat + "<" + elementType.representationIn(nameSpace) + ", " + columns + ", " + rows + ">";
    }
}
