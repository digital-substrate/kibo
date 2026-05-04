package com.digitalsubstrate.viper.dsm;

import java.util.ArrayList;
import java.util.UUID;

public final class DSMAttachmentFunctionPool {

    public final UUID uuid;
    public final String name;
    public final ArrayList<DSMAttachmentFunction> functions;
    public final String documentation;

    public DSMAttachmentFunctionPool(UUID uuid,
                                     String name,
                                     ArrayList<DSMAttachmentFunction> functions,
                                     String documentation) {
        this.uuid = uuid;
        this.name = name;
        this.functions = functions;
        this.documentation = documentation;
    }

}
