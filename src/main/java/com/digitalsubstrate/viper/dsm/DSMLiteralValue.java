// Copyright (c) Digital Substrate 2026, All rights reserved.

package com.digitalsubstrate.viper.dsm;

public final class DSMLiteralValue extends DSMLiteral {

    public final DSMLiteralDomain domain;
    public final String value;

    public DSMLiteralValue(DSMLiteralDomain domain, String value) {
        this.domain = domain;
        this.value = value;
    }
}
