package com.digitalsubstrate.template;

import com.digitalsubstrate.viper.dsm.DSMAttachmentFunctionPool;

import java.util.ArrayList;

public final class TemplateAttachmentFunctionPool {

    final public DSMAttachmentFunctionPool dsmAttachmentFunctionPool;
    final public ArrayList<TemplateAttachmentFunction> functions;

    public TemplateAttachmentFunctionPool(ArrayList<TemplateAttachmentFunction> functions, DSMAttachmentFunctionPool dsmAttachmentFunctionPool) {
        this.functions = functions;
        this.dsmAttachmentFunctionPool = dsmAttachmentFunctionPool;
    }

    // DSM
    public String getName() {
        return dsmAttachmentFunctionPool.name;
    }

    public String getUuid() {
        return dsmAttachmentFunctionPool.uuid.toString();
    }

    // Components
    public ArrayList<TemplateAttachmentFunction> getFunctions() {
        return functions;
    }

    // Documentation
    public Boolean getHasDocumentation() {
        return !dsmAttachmentFunctionPool.documentation.isEmpty();
    }

    public String getDocumentation() {
        return dsmAttachmentFunctionPool.documentation;
    }

    // Type
    public String getType() {
        return "AttachmentFunctionPool";
    }
}
