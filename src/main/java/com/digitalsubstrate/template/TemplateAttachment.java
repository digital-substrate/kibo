package com.digitalsubstrate.template;

import com.digitalsubstrate.viper.dsm.DSMAttachment;

public final class TemplateAttachment {

    private final boolean isAmbiguous;
    private final DSMAttachment dsmAttachment;
    private final TemplateAttachedKeyType keyType;
    private final TemplateAttachedDocumentType documentType;

    private String identifier;

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

    /**
     * The concept this attachment is keyed on, spelled as a namespace level can hold it.
     *
     * <p>Bare when the concept belongs to the attachment's own namespace, and prefixed by
     * the concept's namespace when it does not — {@code Material} against
     * {@code ModelA_Material}. A namespace level cannot hold a qualified name, so the
     * origin has to be flattened into the level's own name; the question is only whether
     * that happens on principle or on demand.
     *
     * <p>It happens on principle. {@link #getIdentifier()} flattens only when two
     * attachments would otherwise collide, which makes a scope name a function of the
     * whole namespace's attachment set: adding one attachment renames another, and a
     * caller that named the first stops compiling for a change that did not touch it.
     *
     * <p>A template cannot compute this. StringTemplate has no string comparison, so
     * "is this concept mine?" is a question only the model can answer.
     */
    public String getConceptScope() {
        final var concept = dsmAttachment.keyType.typeName;
        final var here = dsmAttachment.typeName.nameSpace;

        if (concept.nameSpace.equals(here) || concept.nameSpace.isGlobal())
            return TemplateTool.uf(concept.name);

        return TemplateTool.uf(concept.nameSpace.name) + "_" + TemplateTool.uf(concept.name);
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
