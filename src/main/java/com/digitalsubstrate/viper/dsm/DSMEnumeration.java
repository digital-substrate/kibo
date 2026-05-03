// Copyright (c) Digital Substrate 2026, All rights reserved.

package com.digitalsubstrate.viper.dsm;

import com.digitalsubstrate.viper.TypeName;

import java.util.ArrayList;
import java.util.UUID;

public final class DSMEnumeration {

    public final TypeName typeName;
    public final ArrayList<DSMEnumerationCase> members;
    public final String documentation;

    public final DSMTypeReference typeReference;
    public final UUID runtimeId;

    public DSMEnumeration(TypeName typeName, ArrayList<DSMEnumerationCase> members, String documentation, UUID runtimeId) {
        this.typeName = typeName;
        this.members = members;
        this.documentation = documentation;

        this.typeReference = new DSMTypeReference(typeName, DSMTypeReferenceDomain.ENUMERATION);
        this.runtimeId = runtimeId;
    }
}
