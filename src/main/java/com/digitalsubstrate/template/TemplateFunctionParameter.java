package com.digitalsubstrate.template;

import com.digitalsubstrate.viper.dsm.DSMFunctionPrototypeParameter;

public final class TemplateFunctionParameter {

    private final DSMFunctionPrototypeParameter dsmFunctionPrototypeParameter;
    private final String type;
    private final String passBy;
    private final String typeSuffix;
    private final String viperValue;
    private final TemplateBindingType bindingType;

    public TemplateFunctionParameter(DSMFunctionPrototypeParameter dsmFunctionPrototypeParameter,
                                     String type,
                                     String passBy,
                                     String typeSuffix,
                                     String viperValue,
                                     TemplateBindingType bindingType) {
        this.dsmFunctionPrototypeParameter = dsmFunctionPrototypeParameter;
        this.type = type;
        this.passBy = passBy;
        this.typeSuffix = typeSuffix;
        this.viperValue = viperValue;
        this.bindingType = bindingType;
    }

    public String getName() {
        return dsmFunctionPrototypeParameter.name;
    }

    public String getPassBy() {
        return passBy;
    }

    // Type
    public String getTypeSuffix() {
        return typeSuffix;
    }

    public String getType() {
        return type;
    }

    // Viper
    public String getViperValue() {
        return viperValue;
    }

    // Binding
    public TemplateBindingType getBindingType() {
        return bindingType;
    }
}
