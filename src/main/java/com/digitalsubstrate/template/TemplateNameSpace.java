package com.digitalsubstrate.template;

import com.digitalsubstrate.viper.NameSpace;

import java.util.ArrayList;

public class TemplateNameSpace {

    public final NameSpace nameSpace;
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
}
