package com.digitalsubstrate.viper.dsm;

public final class DSMAttachmentFunction {

    public final boolean isMutable;
    public final DSMFunctionPrototype prototype;
    public final String documentation;

    public DSMAttachmentFunction(boolean isMutable,
                                 DSMFunctionPrototype prototype,
                                 String documentation) {
        this.isMutable = isMutable;
        this.prototype = prototype;
        this.documentation = documentation;
    }
}
