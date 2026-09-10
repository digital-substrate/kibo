package com.digitalsubstrate.template;

import java.util.ArrayList;

public final class TemplateVariantFunction {

    private final String type;
    private final String typeSuffix;
    private final ArrayList<TemplateType> members;
    private final String dsmType;
    private final TemplateBindingType bindingType;

    public TemplateVariantFunction(String type, String typeSuffix, ArrayList<TemplateType> members,
                                   String dsmType,
                                   TemplateBindingType bindingType) {
        this.type = type;
        this.typeSuffix = typeSuffix;
        this.members = members;
        this.dsmType = dsmType;
        this.bindingType = bindingType;
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

    // Binding
    public TemplateBindingType getBindingType() {
        return bindingType;
    }
}
