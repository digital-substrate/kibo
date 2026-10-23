package com.digitalsubstrate.template;

import com.digitalsubstrate.viper.dsm.DSMAttachmentFunction;

import java.util.ArrayList;

public final class TemplateAttachmentFunction {

    final private DSMAttachmentFunction dsmAttachmentFunction;
    final private String type;
    final private String typeSuffix;
    final private ArrayList<TemplateFunctionParameter> parameters;
    final private String returnViperValue;
    final private TemplatePythonType returnPythonType;

    public TemplateAttachmentFunction(DSMAttachmentFunction dsmAttachmentFunction,
                                      String type,
                                      String typeSuffix,
                                      ArrayList<TemplateFunctionParameter> parameters,
                                      String returnViperValue,
                                      TemplatePythonType returnPythonType) {
       this.dsmAttachmentFunction = dsmAttachmentFunction;
       this.parameters = parameters;
       this.type = type;
       this.typeSuffix = typeSuffix;
       this.returnViperValue = returnViperValue;
       this.returnPythonType = returnPythonType;
    }

    // DSM
    public String getName() {
        return dsmAttachmentFunction.prototype.name;
    }

    // Components
    public ArrayList<TemplateFunctionParameter> getParameters() {
        return parameters;
    }

    // Predicates
    public Boolean getIsVoid() {
        return returnViperValue.equals("ValueVoid");
    }

    // Documentation
    public Boolean getHasDocumentation() {
        return !dsmAttachmentFunction.documentation.isEmpty();
    }

    public String getDocumentation() {
        return dsmAttachmentFunction.documentation;
    }

    // Type
    public String getType() {
        return type;
    }

    public String getTypeSuffix() {
        return typeSuffix;
    }

    // Type
    /** The half of the interface's name that varies: `Mutating` or `Getting`, for a parameter's name. */
    public String getInterfaceType() {
        return dsmAttachmentFunction.isMutable ? "Mutating" : "Getting";
    }

    /** The interface the function takes, as the runtime names it: `CommitAttachmentMutating` or `AttachmentGetting`. */
    public String getInterfaceName() {
        return dsmAttachmentFunction.isMutable ? "CommitAttachmentMutating" : "AttachmentGetting";
    }

    public Boolean getIsMutable() {
        return dsmAttachmentFunction.isMutable;
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
