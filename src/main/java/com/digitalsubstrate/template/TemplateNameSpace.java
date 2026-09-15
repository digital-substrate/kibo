package com.digitalsubstrate.template;

import com.digitalsubstrate.converter.TargetLayout;
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
    public final TemplateDependencies dependencies = new TemplateDependencies();

    public final ArrayList<TemplateConcept> concepts = new ArrayList<>();
    public final ArrayList<TemplateClub> clubs = new ArrayList<>();
    public final ArrayList<TemplateStructure> sortedStructures = new ArrayList<>();
    public final ArrayList<TemplateStructure> structures = new ArrayList<>();
    public final ArrayList<TemplateEnumeration> enumerations = new ArrayList<>();
    public final ArrayList<TemplateAttachment> attachments = new ArrayList<>();

    private final TemplateDefinitions model;
    private final TemplateIncludePaths include;

    public TemplateNameSpace(NameSpace nameSpace, TemplateDefinitions model, TargetLayout layout) {
        this.nameSpace = nameSpace;
        this.model = model;
        this.include = new TemplateIncludePaths(layout, TargetLayout.Scope.UNIT, nameSpace.name);
    }

    /**
     * The whole model, for what a unit does not own.
     *
     * <p>A unit template needs the banner, and it needs to reach artefacts that have not
     * been split yet — those still live under {@code -n}, so {@code u.model.include.Data}
     * is where Data is until Data itself becomes per-unit, at which point the template
     * switches to {@code u.include.Data}. Making that switch an edit, at the moment the
     * artefact moves, is the point: it is visible in a diff instead of resolving
     * differently depending on what else has been migrated.
     */
    public TemplateDefinitions getModel() {
        return model;
    }

    /** Where this unit's own artefacts are found: {@code <u.include.Data>}. */
    public TemplateIncludePaths getInclude() {
        return include;
    }

    public String getName() {
        return nameSpace.name;
    }

    public TemplateDependencies getDependencies() {
        return dependencies;
    }
}
