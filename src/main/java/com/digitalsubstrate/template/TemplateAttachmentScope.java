package com.digitalsubstrate.template;

import java.util.ArrayList;

/**
 * The attachments a unit declares on one concept, grouped.
 *
 * <p>A unit's attachments are a flat list, and for a target whose scope is a namespace that
 * is enough: three {@code namespace Link} blocks merge into one, so a template can open a
 * scope per attachment and let the language join them. For a target whose scope is a class
 * or an object they do not merge — three {@code class Link} definitions leave one, and the
 * first two are lost with nothing said.
 *
 * <p>So the grouping is the model's to do. StringTemplate has no group-by and no string
 * comparison, which makes "same concept?" a question only the model can answer, and
 * {@link TemplateAttachment#getConceptScope()} already answers it for the name.
 */
public final class TemplateAttachmentScope {

    private final String name;
    private final TemplateAttachedKeyType keyType;

    public final ArrayList<TemplateAttachment> attachments = new ArrayList<>();

    public TemplateAttachmentScope(String name, TemplateAttachedKeyType keyType) {
        this.name = name;
        this.keyType = keyType;
    }

    /** The concept, spelled as a scope of this unit can hold it. */
    public String getName() {
        return name;
    }

    public TemplateAttachedKeyType getKeyType() {
        return keyType;
    }

    public ArrayList<TemplateAttachment> getAttachments() {
        return attachments;
    }
}
