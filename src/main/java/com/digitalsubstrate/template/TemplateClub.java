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

    // Python
    public TemplatePythonType getPythonType() {
        final var proxy = dsmClub.typeName.nameSpace.name + "_" + dsmClub.typeName.name;
        return new TemplatePythonType(proxy, typeSuffix);
    }

}