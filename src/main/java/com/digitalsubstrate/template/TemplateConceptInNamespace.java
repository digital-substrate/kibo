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

    /**
     * The member's key, spelled as a target that makes a unit a module writes it.
     *
     * <p>The same pair as {@link TemplateConcept#getParentBindingInNamespace()}: a scope
     * target writes {@code Core::ThingKey}, a module target writes {@code core.ThingKey}.
     * Only the model can tell the two cases apart, because telling them apart means comparing
     * two namespaces.
     */
    public String getBindingInNamespace() {
        return concept.getNamespace().equals(namespace)
            ? concept.getName() + "Key"
            : TemplateTool.lsc(concept.getNamespace()) + "." + concept.getName() + "Key";
    }

    public String getAsNameInNamespace() {
        if (concept.getNamespace().equals(namespace))  {
            return concept.getName();
        } else {
            return concept.getNamespace() + concept.getName();
        }
    }
}

