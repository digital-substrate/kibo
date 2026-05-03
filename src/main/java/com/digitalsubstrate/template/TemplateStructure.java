package com.digitalsubstrate.template;

import com.digitalsubstrate.viper.dsm.DSMStructure;

import java.util.ArrayList;

public final class TemplateStructure {

    private final DSMStructure dsmStructure;
    private final String type;
    private final String typeSuffix;
    private final ArrayList<TemplateStructureField> fields = new ArrayList<>();
    private final boolean isMovable;

    public TemplateStructure(DSMStructure dsmStructure, String type, String typeSuffix, boolean isMovable) {
        this.dsmStructure = dsmStructure;
        this.type = type;
        this.typeSuffix = typeSuffix;
        this.isMovable = isMovable;
    }

    // DSM
    public DSMStructure getDsmStructure() {
        return dsmStructure;
    }

    // Components
    public ArrayList<TemplateStructureField> getFields() {
        return fields;
    }

    // Predicates
    public boolean getIsMovable() {
        return isMovable;
    }

    // Namespace
    public String getNamespace() {
        return dsmStructure.typeName.nameSpace.name;
    }

    public String getName() {
        return dsmStructure.typeName.name;
    }

    // Runtime Id
    public String getRuntimeId() {
        return dsmStructure.runtimeId.toString().toLowerCase();
    }

    // Documentation
    public Boolean getHasDocumentation() {
        return !dsmStructure.documentation.isEmpty();
    }

    public String getDocumentation() {
        return dsmStructure.documentation;
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
        return "TypeStructure";
    }

    public String getViperValue() {
        return "ValueStructure";
    }

    // Python
    public TemplatePythonType getPythonType() {
        final var proxy = dsmStructure.typeName.nameSpace.name + "_" + dsmStructure.typeName.name;
        return new TemplatePythonType(proxy, typeSuffix);
    }

}
