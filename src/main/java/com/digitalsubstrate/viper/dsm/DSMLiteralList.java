// Copyright (c) Digital Substrate 2026, All rights reserved.

package com.digitalsubstrate.viper.dsm;

import java.util.ArrayList;

public final class DSMLiteralList extends DSMLiteral {

    public final ArrayList<DSMLiteral> members;

    public DSMLiteralList(ArrayList<DSMLiteral> members) {
        this.members = members;
    }
}
