package com.digitalsubstrate.viper.dsm;

import com.digitalsubstrate.viper.TypeName;

import java.util.ArrayList;
import java.util.UUID;

public final class DSMStructure {

    public final TypeName typeName;
    public final ArrayList<DSMStructureField> fields;
    public final String documentation;

    public final DSMTypeReference typeReference;
    public final UUID runtimeId;

    public DSMStructure(TypeName typeName, ArrayList<DSMStructureField> fields, String documentation, UUID runtimeId) {
        this.typeName = typeName;
        this.fields = fields;
        this.documentation = documentation;

        this.typeReference = new DSMTypeReference(typeName, DSMTypeReferenceDomain.STRUCTURE);
        this.runtimeId = runtimeId;
    }

    public String getName() {
        return typeName.name;
    }

    public String getRepresentation() {
        return typeName.representation();
    }
}
