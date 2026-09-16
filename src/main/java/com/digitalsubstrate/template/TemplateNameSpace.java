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

    /**
     * The same attachments, grouped by the concept they are keyed on.
     *
     * <p>Both lists, because both are true and a target needs one or the other: a scope that
     * merges wants the flat list, a scope that does not wants the grouping. Deriving one from
     * the other in a template is what StringTemplate cannot do.
     */
    public final ArrayList<TemplateAttachmentScope> attachmentScopes = new ArrayList<>();

    /** Fill {@link #attachmentScopes} from {@link #attachments}, in order of first sighting. */
    public void groupAttachments() {
        for (var attachment : attachments) {
            final var name = attachment.getConceptScope();
            var scope = attachmentScopes.stream()
                .filter(candidate -> candidate.getName().equals(name))
                .findFirst().orElse(null);

            if (scope == null) {
                scope = new TemplateAttachmentScope(name, attachment.getKeyType());
                attachmentScopes.add(scope);
            }
            scope.attachments.add(attachment);
        }
    }

    public ArrayList<TemplateAttachmentScope> getAttachmentScopes() {
        return attachmentScopes;
    }

    /**
     * Every name this unit declares, as the target writes it.
     *
     * <p>The four lists concatenated, with a concept's and a club's {@code Key} suffix
     * applied — which is the one thing a template cannot do for itself: StringTemplate has
     * no concatenation across lists, so joining four possibly-empty groups with a separator
     * means writing the four cases and their three commas by hand, once per template that
     * needs it.
     *
     * <p>And several need it: an export list, an import of a unit's own names from a
     * sibling module, a registry of its classes. All three are the same list.
     */
    public ArrayList<String> getExported() {
        final var result = new ArrayList<String>();
        for (var concept : concepts)
            result.add(concept.getName() + "Key");
        for (var club : clubs)
            result.add(club.getName() + "Key");
        for (var enumeration : enumerations)
            result.add(enumeration.getName());
        for (var structure : structures)
            result.add(structure.getName());

        return result;
    }

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
