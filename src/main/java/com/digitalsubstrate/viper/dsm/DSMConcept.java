package com.digitalsubstrate.viper.dsm;

import com.digitalsubstrate.viper.TypeName;

import java.util.UUID;

public final class DSMConcept {

    public final TypeName typeName;
    public final DSMTypeReference parent;
    public final String documentation;
    public final DSMTypeReference typeReference;
    public final UUID runtimeId;

    public DSMConcept(TypeName typeName, DSMTypeReference parent, String documentation, UUID runtimeId) {
        this.typeName = typeName;
        this.parent = parent;
        this.documentation = documentation;

        this.typeReference = new DSMTypeReference(typeName, DSMTypeReferenceDomain.CONCEPT);
        this.runtimeId = runtimeId;
    }
}


