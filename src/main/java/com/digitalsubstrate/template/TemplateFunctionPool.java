package com.digitalsubstrate.template;

import com.digitalsubstrate.viper.dsm.DSMFunctionPool;

import java.util.ArrayList;

public final class TemplateFunctionPool {

    final public DSMFunctionPool dsmFunctionPool;
    final public ArrayList<TemplateFunction> functions;

    public TemplateFunctionPool(ArrayList<TemplateFunction> functions, DSMFunctionPool dsmFunctionPool) {
        this.functions = functions;
        this.dsmFunctionPool = dsmFunctionPool;
    }

    public String getName() {
        return dsmFunctionPool.name;
    }

    public String getUuid() {
        return dsmFunctionPool.uuid.toString();
    }

    // Components
    public ArrayList<TemplateFunction> getFunctions() {
        return functions;
    }

    // Documentation
    public Boolean getHasDocumentation() {
        return !dsmFunctionPool.documentation.isEmpty();
    }

    public String getDocumentation() {
        return dsmFunctionPool.documentation;
    }

    // Type
    public String getType() {
        return "FunctionPool";
    }
}
