package com.digitalsubstrate.viper.dsm;

public final class DSMFunction {

    public final DSMFunctionPrototype prototype;
    public final String documentation;

    public DSMFunction(DSMFunctionPrototype prototype, String documentation) {
        this.prototype = prototype;
        this.documentation = documentation;
    }
}
