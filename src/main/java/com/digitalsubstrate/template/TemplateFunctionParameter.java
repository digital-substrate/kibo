package com.digitalsubstrate.template;

import com.digitalsubstrate.viper.dsm.DSMFunctionPrototypeParameter;

public final class TemplateFunctionParameter {

    private final DSMFunctionPrototypeParameter dsmFunctionPrototypeParameter;
    private final String type;
    private final String passBy;
    private final String typeSuffix;
    private final String viperValue;
    private final TemplatePythonType pythonType;

    public TemplateFunctionParameter(DSMFunctionPrototypeParameter dsmFunctionPrototypeParameter,
                                     String type,
                                     String passBy,
                                     String typeSuffix,
                                     String viperValue,
                                     TemplatePythonType pythonType) {
        this.dsmFunctionPrototypeParameter = dsmFunctionPrototypeParameter;
        this.type = type;
        this.passBy = passBy;
        this.typeSuffix = typeSuffix;
        this.viperValue = viperValue;
        this.pythonType = pythonType;
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

    // Python
    public TemplatePythonType getPythonType() {
        return pythonType;
    }
}
