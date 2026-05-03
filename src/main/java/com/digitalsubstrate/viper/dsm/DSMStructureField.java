// Copyright (c) Digital Substrate 2026, All rights reserved.

package com.digitalsubstrate.viper.dsm;

public final class DSMStructureField {

    public final String name;
    public final DSMType type;
    public final DSMLiteral defaultValue;
    public final String documentation;

    public DSMStructureField(String name, DSMType type, DSMLiteral defaultValue, String documentation) {
        this.name = name;
        this.type = type;
        this.defaultValue = defaultValue;
        this.documentation = documentation;
    }
}
