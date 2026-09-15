package com.digitalsubstrate.template;

import com.digitalsubstrate.viper.dsm.DSMEnumeration;
import com.digitalsubstrate.viper.dsm.DSMEnumerationCase;

import java.util.ArrayList;

public final class TemplateEnumeration {

    private final DSMEnumeration dsmEnumeration;
    private final String type;
    private final String typeSuffix;

    public TemplateEnumeration(DSMEnumeration dsmEnumeration, String type, String typeSuffix) {
        this.dsmEnumeration = dsmEnumeration;
        this.type = type;
        this.typeSuffix = typeSuffix;
    }

    // DSM
    public DSMEnumeration getDsmEnumeration() {
        return dsmEnumeration;
    }

    // Components
    public ArrayList<DSMEnumerationCase> getMembers() {
        return dsmEnumeration.members;
    }

    // Namespace
    public String getNamespace() {
        return dsmEnumeration.typeName.nameSpace.name;
    }

    public String getName() {
        return dsmEnumeration.typeName.name;
    }

    // Runtime Id
    public String getRuntimeId() {
        return dsmEnumeration.runtimeId.toString().toLowerCase();
    }

    // Documentation
    public Boolean getHasDocumentation() {
        return !dsmEnumeration.documentation.isEmpty();
    }

    public String getDocumentation() {
        return dsmEnumeration.documentation;
    }

    // Type
    /** The DSM name of this entity — what the model calls it, whatever the target. */
    public String getDsmType() {
        return dsmEnumeration.typeName.representation();
    }

    public String getType() {
        return type;
    }

    public String getTypeSuffix() {
        return typeSuffix;
    }

    // Viper
    public String getViperType() {
        return "TypeEnumeration";
    }

    public String getViperValue() {
        return "ValueEnumeration";
    }

    // Binding
    public TemplateBindingType getBindingType() {
        // Ce type-ci est déclaré par cette unité-ci, donc vu d'elle il s'écrit nu : c'est
        // le seul cas où les deux orthographes se déduisent l'une de l'autre sans rien
        // savoir de plus.
        final var name = dsmEnumeration.typeName.name;
        final var proxy = dsmEnumeration.typeName.nameSpace.name + "_" + name;
        return new TemplateBindingType(proxy, typeSuffix, proxy, name, true, true);
    }
}