package com.digitalsubstrate.template;

import java.util.ArrayList;

public final class TemplateVariantFunction {

    private final String type;
    private final String typeSuffix;
    private final ArrayList<TemplateType> members;
    private final String dsmType;
    private final TemplatePythonType pythonType;
    private final ArrayList<TemplatePythonType> pythonMembers;

    public TemplateVariantFunction(String type, String typeSuffix, ArrayList<TemplateType> members,
                                   String dsmType,
                                   TemplatePythonType pythonType, ArrayList<TemplatePythonType> pythonMembers) {
        this.type = type;
        this.typeSuffix = typeSuffix;
        this.members = members;
        this.dsmType = dsmType;
        this.pythonType = pythonType;
        this.pythonMembers = pythonMembers;
    }

    // DSM
    public String getDsmType() {
        return dsmType;
    }

    // Components
    public ArrayList<TemplateType> getMembers() {
        return members;
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
        return "TypeVariant";
    }

    public String getViperValue() {
        return "ValueVariant";
    }

    // Python
    public TemplatePythonType getPythonType() {
        return pythonType;
    }

    public ArrayList<TemplatePythonType> getPythonMembers() {
        return pythonMembers;
    }
}
