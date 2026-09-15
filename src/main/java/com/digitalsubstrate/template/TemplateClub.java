package com.digitalsubstrate.template;

import com.digitalsubstrate.viper.dsm.DSMClub;
import com.digitalsubstrate.viper.TypeName;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

public final class TemplateClub {

    private final DSMClub dsmClub;
    private final String type;
    private final String typeSuffix;
    private final ArrayList<TemplateConcept> members;

    private ArrayList<TemplateConceptInNamespace> membersInNamespace;
    private ArrayList<TemplateConcept> memberDescendants;

    public TemplateClub(DSMClub dsmClub, String type, String typeSuffix, ArrayList<TemplateConcept> members) {
        this.dsmClub = dsmClub;
        this.type = type;
        this.typeSuffix = typeSuffix;
        this.members = members;
    }

    // DSM
    public DSMClub getDsmClub() {
        return dsmClub;
    }

    // Namespace
    public String getNamespace() {
        return dsmClub.typeName.nameSpace.name;
    }

    public String getName() {
        return dsmClub.typeName.name;
    }

    // Runtime Id
    public String getRuntimeId() {
        return dsmClub.runtimeId.toString().toLowerCase();
    }

    // Documentation
    public Boolean getHasDocumentation() {
        return !dsmClub.documentation.isEmpty();
    }

    public String getDocumentation() {
        return dsmClub.documentation;
    }

    // Components
    public ArrayList<TemplateConcept> getMembers() {
        return members;
    }

    public ArrayList<TemplateConceptInNamespace> getMembersInNamespace() {
        if (membersInNamespace == null) {
            membersInNamespace = new ArrayList<>();
            for (var concept : members)
                membersInNamespace.add(new TemplateConceptInNamespace(concept, getNamespace()));
        }
        return membersInNamespace;
    }

    public ArrayList<TemplateConcept> getMemberDescendants() {
        if (memberDescendants == null) {
            memberDescendants = new ArrayList<>();
            final var map = new HashMap<TypeName, TemplateConcept>();
            for (var member : members) {
                for (var concept : member.getDescendants()) {
                    if (map.get(concept.getDsmConcept().typeName) == null) {
                        map.put(concept.getDsmConcept().typeName, concept);
                        memberDescendants.add(concept);
                    }
                }
            }
            memberDescendants.sort(Comparator.comparing(TemplateConcept::getName));
        }
        return memberDescendants;
    }

    // Type
    /**
     * The DSM name of this club — what the model calls it. Not the same as
     * {@link #getType()}, which is the C++ key type and carries a {@code Key} suffix the
     * DSM does not.
     */
    public String getDsmType() {
        return dsmClub.typeName.representation();
    }

    public String getType() {
        return type;
    }

    public String getTypeSuffix() {
        return typeSuffix;
    }

    // Viper
    public String getViperType() {
        return "TypeKey";
    }

    public String getViperValue() {
        return "ValueKey";
    }

    // Binding
    public TemplateBindingType getBindingType() {
        // Ce type-ci est déclaré par cette unité-ci, donc vu d'elle il s'écrit nu : c'est
        // le seul cas où les deux orthographes se déduisent l'une de l'autre sans rien
        // savoir de plus.
        final var name = dsmClub.typeName.name;
        final var proxy = dsmClub.typeName.nameSpace.name + "_" + name;
        return new TemplateBindingType(proxy, typeSuffix, proxy + "Key", name + "Key", true);
    }

}