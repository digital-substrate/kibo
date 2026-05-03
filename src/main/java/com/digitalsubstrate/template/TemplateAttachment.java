package com.digitalsubstrate.template;

import com.digitalsubstrate.viper.dsm.DSMAttachment;

public final class TemplateAttachment {

    private final boolean isAmbiguous;
    private final DSMAttachment dsmAttachment;
    private final TemplateAttachedKeyType keyType;
    private final TemplateAttachedDocumentType documentType;

    private String identifier;
    private String pythonIdentifier;

    public TemplateAttachment(boolean isAmbiguous,
                              DSMAttachment dsmAttachment,
                              TemplateAttachedKeyType keyType,
                              TemplateAttachedDocumentType documentType) {

        this.isAmbiguous = isAmbiguous;
        this.dsmAttachment = dsmAttachment;
        this.keyType = keyType;
        this.documentType = documentType;
    }

    // DSM
    public DSMAttachment getDsmAttachment() {
        return dsmAttachment;
    }

    public String getRepresentation() {
        return dsmAttachment.representation();
    }

    public String getIdentifier() {
        if (identifier == null) {
            identifier = TemplateTool.uf(dsmAttachment.keyType.typeName.name) + "_" + TemplateTool.uf(dsmAttachment.typeName.name);
            if (isAmbiguous)
                identifier = TemplateTool.uf(dsmAttachment.keyType.typeName.nameSpace.name) + "_" + identifier;
        }
        return identifier;
    }

    // Namespace
    public String getNamespace() {
        return dsmAttachment.typeName.nameSpace.name;
    }

    public String getName() {
        return dsmAttachment.typeName.name;
    }

    // RuntimeId
    public String getRuntimeId() {
        return dsmAttachment.runtimeId.toString().toLowerCase();
    }

    // Documentation
    public Boolean getHasDocumentation() {
        return !dsmAttachment.documentation.isEmpty();
    }

    public String getDocumentation() {
        return dsmAttachment.documentation;
    }

    // Type
    public TemplateAttachedKeyType getKeyType() {
        return keyType;
    }

    public TemplateAttachedDocumentType getDocumentType() {
        return documentType;
    }
}
