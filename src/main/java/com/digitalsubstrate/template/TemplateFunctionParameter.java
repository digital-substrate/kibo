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

    /** The name as this target spells it: the DSM name, unless the project spells it otherwise. */
    public String getName() {
        return TemplateTool.spell(dsmFunctionPrototypeParameter.name);
    }

    /** The DSM name, the one the runtime knows: write it where a name is sent to the runtime. */
    public String getDsmName() {
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
