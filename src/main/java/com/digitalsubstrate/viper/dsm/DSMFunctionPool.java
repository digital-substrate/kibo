package com.digitalsubstrate.viper.dsm;

import java.util.ArrayList;
import java.util.UUID;

public final class DSMFunctionPool {

    public final UUID uuid;
    public final String name;
    public final ArrayList<DSMFunction> functions;
    public final String documentation;

    public DSMFunctionPool(UUID uuid, String name, ArrayList<DSMFunction> functions, String documentation) {
        this.uuid = uuid;
        this.name = name;
        this.functions = functions;
        this.documentation = documentation;
    }

}
