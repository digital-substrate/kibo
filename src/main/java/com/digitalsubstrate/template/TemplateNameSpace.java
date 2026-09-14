package com.digitalsubstrate.template;

import com.digitalsubstrate.viper.NameSpace;

import java.util.ArrayList;

public class TemplateNameSpace {

    public final NameSpace nameSpace;

    /**
     * The other namespaces this one references, in the order they must be emitted.
     *
     * <p>The converter has always computed this — it is what the topological sort is
     * built from — and has always discarded it, keeping only the order. For a target
     * where a namespace is a location rather than a prefix, this set <em>is</em> the
     * import list, and nothing else can produce it: a template can see what a type is
     * called but not which other namespaces its own reaches.
     *
     * <p>Populated in topological order, so a dependency is always already built when
     * the namespace that needs it is.
     */
    public final ArrayList<TemplateNameSpace> dependencies = new ArrayList<>();

    public final ArrayList<TemplateConcept> concepts = new ArrayList<>();
    public final ArrayList<TemplateClub> clubs = new ArrayList<>();
    public final ArrayList<TemplateStructure> sortedStructures = new ArrayList<>();
    public final ArrayList<TemplateStructure> structures = new ArrayList<>();
    public final ArrayList<TemplateEnumeration> enumerations = new ArrayList<>();
    public final ArrayList<TemplateAttachment> attachments = new ArrayList<>();

    public TemplateNameSpace(NameSpace nameSpace) {
        this.nameSpace = nameSpace;
    }

    public String getName() {
        return nameSpace.name;
    }

    public ArrayList<TemplateNameSpace> getDependencies() {
        return dependencies;
    }
}
