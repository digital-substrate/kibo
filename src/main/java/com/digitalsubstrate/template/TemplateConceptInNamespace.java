package com.digitalsubstrate.template;

public class TemplateConceptInNamespace {

    private final TemplateConcept concept;
    private final String namespace;

    public TemplateConceptInNamespace(TemplateConcept concept, String namespace) {
        this.concept = concept;
        this.namespace = namespace;
    }

    // Component
    public TemplateConcept getConcept() {
        return concept;
    }

    // Namespace
    public String getNameInNamespace() {
        if (concept.getNamespace().equals(namespace))  {
            return concept.getName();
        } else {
            return concept.getNamespace() + "::" + concept.getName();
        }
    }

    public String getAsNameInNamespace() {
        if (concept.getNamespace().equals(namespace))  {
            return concept.getName();
        } else {
            return concept.getNamespace() + concept.getName();
        }
    }
}

