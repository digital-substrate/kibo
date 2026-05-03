package com.digitalsubstrate.template;

import com.digitalsubstrate.viper.dsm.DSMConcept;

import java.util.ArrayList;
import java.util.Comparator;

public final class TemplateConcept {

    private final DSMConcept dsmConcept;
    private final String type;
    private final String typeSuffix;
    private final ArrayList<TemplateAttachment> attachments;

    private TemplateConcept parent;
    private ArrayList<TemplateConcept> children;

    private ArrayList<TemplateConcept> descendants;
    private ArrayList<TemplateConcept> strictDescendants;
    private ArrayList<TemplateConceptInNamespace> strictDescendantsInNamespace;

    public TemplateConcept(DSMConcept dsmConcept, String type, String typeSuffix, ArrayList<TemplateAttachment> attachments) {
        this.dsmConcept = dsmConcept;
        this.type = type;
        this.typeSuffix = typeSuffix;
        this.attachments = attachments;
        this.parent = null;
        this.children = null;
    }

    // DSM
    public DSMConcept getDsmConcept() {
        return dsmConcept;
    }

    // Components
    public TemplateConcept getParent() {
        return parent;
    }

    public String getParentNameInNamespace() {
        return parent.dsmConcept.typeName.representationIn(dsmConcept.typeName.nameSpace);
    }

    public void setParent(TemplateConcept parent) {
        this.parent = parent;
    }

    public void setChildren(ArrayList<TemplateConcept> children) {
        this.children = children;
    }

    public ArrayList<TemplateConcept> getChildren() {
        return children;
    }

    public ArrayList<TemplateConcept> getDescendants() {
        if (descendants == null) {
            descendants = new ArrayList<>();
            collectDescendants(this, descendants);
            descendants.sort(Comparator.comparing(TemplateConcept::getName));
        }
        return descendants;
    }

    public ArrayList<TemplateConcept> getStrictDescendants() {
        if (strictDescendants == null) {
            strictDescendants = new ArrayList<>();
            collectDescendants(this, strictDescendants);
            strictDescendants.remove(0);
            strictDescendants.sort(Comparator.comparing(TemplateConcept::getName));
        }
        return strictDescendants;
    }

    public ArrayList<TemplateConceptInNamespace> getStrictDescendantsInNamespace() {
        if (strictDescendantsInNamespace == null) {
            strictDescendantsInNamespace = new ArrayList<>();
            final var descendants = new ArrayList<TemplateConcept>();
            collectDescendants(this, descendants);
            descendants.remove(0);

            for (var concept : descendants) {
                strictDescendantsInNamespace.add(new TemplateConceptInNamespace(concept, getNamespace()));
            }
            strictDescendantsInNamespace.sort(Comparator.comparing(TemplateConceptInNamespace::getNameInNamespace));
            return strictDescendantsInNamespace;
        }
        return strictDescendantsInNamespace;
    }

    private void collectDescendants(TemplateConcept concept, ArrayList<TemplateConcept> acc) {
        acc.add(concept);
        for (TemplateConcept child : concept.children) {
            collectDescendants(child, acc);
        }
    }

    // Namespace
    public String getNamespace() {
        return dsmConcept.typeName.nameSpace.name;
    }

    public String getName() {
        return dsmConcept.typeName.name;
    }

    // Runtime Id
    public String getRuntimeId() {
        return dsmConcept.runtimeId.toString().toLowerCase();
    }

    // Documentation
    public Boolean getHasDocumentation() {
        return !dsmConcept.documentation.isEmpty();
    }

    public String getDocumentation() {
        return dsmConcept.documentation;
    }

    // Type
    public String getType() {
        return type;
    }

    public String getTypeSuffix() {
        return typeSuffix;
    }

    // Attachments
    public ArrayList<TemplateAttachment> getAttachments() {
        return attachments;
    }

    // Viper
    public String getViperType() {
        return "TypeKey";
    }

    public String getViperValue() {
        return "ValueKey";
    }

    // Python
    public TemplatePythonType getPythonType() {
        final var proxy = dsmConcept.typeName.nameSpace.name + "_" + dsmConcept.typeName.name;
        return new TemplatePythonType(proxy, typeSuffix);
    }
}
