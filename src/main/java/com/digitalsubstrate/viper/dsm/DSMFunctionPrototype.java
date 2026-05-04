package com.digitalsubstrate.viper.dsm;

import java.util.ArrayList;

public final class DSMFunctionPrototype {

    public final String name;
    public final ArrayList<DSMFunctionPrototypeParameter> parameters;
    public final DSMType returnType;

    public DSMFunctionPrototype(String name, ArrayList<DSMFunctionPrototypeParameter> parameters, DSMType returnType) {
        this.name = name;
        this.parameters = parameters;
        this.returnType = returnType;
    }
}
