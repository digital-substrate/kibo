package com.digitalsubstrate.template;

import com.digitalsubstrate.viper.dsm.DSMConcept;

import java.util.ArrayList;
import java.util.Comparator;

public final class TemplateConcept {
    private String qualified;

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
        final var parentName = parent.dsmConcept.typeName;
        if (parentName.nameSpace.equals(dsmConcept.typeName.nameSpace))
            return parentName.name;
        return TemplateTool.lsc(parentName.nameSpace.name) + "::" + parentName.name;
    }

    /**
     * The parent's key, spelled as a target that makes a unit a module writes it.
     *
     * <p>{@link #getParentNameInNamespace()} answers for a target where a unit is a scope —
     * {@code Core::ThingKey} — and a module target needs the other spelling,
     * {@code core.ThingKey}. The rule is the one the binding already applies to every other
     * type: bare inside the declaring unit, prefixed by the module elsewhere. Stated here
     * rather than composed in a template, because StringTemplate cannot compare two
     * namespaces and so cannot know which of the two cases it is in.
     */
    public String getParentBindingInNamespace() {
        final var parentName = parent.dsmConcept.typeName;
        final var here = dsmConcept.typeName.nameSpace;

        return parentName.nameSpace.equals(here)
            ? parentName.name + "Key"
            : TemplateTool.lsc(parentName.nameSpace.name) + "." + parentName.name + "Key";
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

    /**
     * Every ancestor, the parent first and the root last, each named as seen from this
     * concept's namespace. An ancestor is declared before its descendants and in a namespace
     * this one depends on, so a descendant's generated code can name every one of them -- a key
     * widens to any ancestor, not only to its parent.
     */
    public ArrayList<TemplateConceptInNamespace> getStrictAncestorsInNamespace() {
        final var ancestors = new ArrayList<TemplateConceptInNamespace>();
        for (var ancestor = parent; ancestor != null; ancestor = ancestor.parent)
            ancestors.add(new TemplateConceptInNamespace(ancestor, getNamespace()));
        return ancestors;
    }

    /**
     * Whether a strict descendant lives in another namespace. The DSM keeps namespaces acyclic,
     * and a descendant's namespace depends on its parent's: the parent's generated code must not
     * name it. A template naming every concept a key may designate names the local ones only,
     * and widens to the base key class when this is true.
     */
    public Boolean getHasForeignDescendants() {
        return getStrictDescendantsInNamespace().stream().anyMatch(d -> !d.getIsLocal());
    }

    private void collectDescendants(TemplateConcept concept, ArrayList<TemplateConcept> acc) {
        acc.add(concept);
        for (TemplateConcept child : concept.children) {
            collectDescendants(child, acc);
        }
    }

    // Namespace
    public String getNamespace() {
        return TemplateTool.spell(dsmConcept.typeName.nameSpace.name);
    }

    /** The name as this target spells it: the DSM name, unless the project spells it otherwise. */
    public String getName() {
        return TemplateTool.spell(dsmConcept.typeName.name);
    }

    /** The DSM name, the one the runtime knows: write it where a name is sent to the runtime. */
    public String getDsmName() {
        return dsmConcept.typeName.name;
    }

    /**
     * Whether the name is already what {@code format="usnake"} makes of it ({@code RGB},
     * {@code E}): a name written that way for something else — a constant beside the type —
     * would be the type's own name.
     */
    public boolean getNameIsUpperSnake() {
        return TemplateTool.isUpperSnake(getName());
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
    /**
     * The DSM name of this concept — what the model calls it. Not the same as
     * {@link #getType()}, which is the C++ key type and carries a {@code Key} suffix the
     * DSM does not.
     */
    public String getDsmType() {
        return dsmConcept.typeName.representation();
    }

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

    // Binding
    /**
     * The type as the binding writes it. This unit declares it, so from inside it the type is
     * written bare: the one case where the two spellings follow from each other with nothing
     * more to know. {@code qualified} is the converter's, written from outside every unit and
     * so qualified by its module, as for a field that names the type; a native binding has no
     * binding space and none to give.
     */
    public TemplateBindingType getBindingType() {
        final var name = dsmConcept.typeName.name;
        final var proxy = dsmConcept.typeName.nameSpace.name + "_" + name;
        final var bare = name + "Key";
        return new TemplateBindingType(proxy, typeSuffix, proxy + "Key", bare, true, true, bare,
                                       qualified != null ? qualified : bare);
    }

    public TemplateConcept withQualified(String qualified) {
        this.qualified = qualified;
        return this;
    }
}
