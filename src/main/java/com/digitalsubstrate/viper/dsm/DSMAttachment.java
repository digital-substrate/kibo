package com.digitalsubstrate.viper.dsm;

import com.digitalsubstrate.viper.NameSpace;
import com.digitalsubstrate.viper.TypeName;

import java.util.UUID;

public final class DSMAttachment {

    public final TypeName typeName;
    public final DSMTypeReference keyType;
    public final DSMType documentType;
    public final String documentation;
    public final UUID runtimeId;

    public DSMAttachment(TypeName typeName, DSMTypeReference keyType, DSMType documentType, String documentation, UUID runtimeId) {
        this.typeName = typeName;
        this.keyType = keyType;
        this.documentType = documentType;
        this.documentation = documentation;
        this.runtimeId = runtimeId;
    }

    public String getIdentifier() {
        return keyType.representation() + "." + typeName.name;
    }

    public String representation() {
        return DSMLexicon.Attachment
                + "<" + keyType.representationIn(typeName.nameSpace)
                + ", "
                + documentType.representationIn(typeName.nameSpace) + "> "
                + typeName.representation();
    }

    public String representationIn(NameSpace nameSpace) {
        return DSMLexicon.Attachment
                + "<" + keyType.representationIn(nameSpace)
                + ", "
                + documentType.representationIn(nameSpace) + "> "
                + typeName.representation();
    }
}


