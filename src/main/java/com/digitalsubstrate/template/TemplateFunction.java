package com.digitalsubstrate.template;

import com.digitalsubstrate.viper.dsm.DSMFunction;

import java.util.ArrayList;

public final class TemplateFunction {

    final private DSMFunction dsmFunction;
    final private String type;
    final private String typeSuffix;
    final private ArrayList<TemplateFunctionParameter> parameters;
    final private String returnViperValue;
    final private TemplatePythonType returnPythonType;

    public TemplateFunction(DSMFunction dsmFunction,
                            String type,
                            String typeSuffix,
                            ArrayList<TemplateFunctionParameter> parameters ,
                            String returnViperValue,
                            TemplatePythonType returnPythonType) {
        this.dsmFunction = dsmFunction;
        this.type = type;
        this.typeSuffix = typeSuffix;
        this.parameters = parameters;
        this.returnViperValue = returnViperValue;
        this.returnPythonType = returnPythonType;
    }

    public String getName() {
        return this.dsmFunction.prototype.name;
    }

    // Component
    public ArrayList<TemplateFunctionParameter> getParameters() {
        return parameters;
    }

    // Predicate
    public Boolean getIsVoid() {
        return returnViperValue.equals("ValueVoid");
    }

    // Documentation
    public Boolean getHasDocumentation() {
        return !dsmFunction.documentation.isEmpty();
    }

    public String getDocumentation() {
        return dsmFunction.documentation;
    }

    // Type
    public String getType() {
        return type;
    }

    public String getTypeSuffix() {
        return typeSuffix;
    }

    // Viper
    public String getReturnViperValue() {
        return returnViperValue;
    }

    // Python
    public TemplatePythonType getReturnPythonType() {
        return returnPythonType;
    }

}
