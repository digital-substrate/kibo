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
        return TemplateTool.spell(dsmEnumeration.typeName.nameSpace.name);
    }

    /** The name as this target spells it: the DSM name, unless the project spells it otherwise. */
    public String getName() {
        return TemplateTool.spell(dsmEnumeration.typeName.name);
    }

    /** The DSM name, the one the runtime knows: write it where a name is sent to the runtime. */
    public String getDsmName() {
        return dsmEnumeration.typeName.name;
    }

    /**
     * Whether the name is already what {@code format="usnake"} makes of it ({@code RGB},
     * {@code E}): a name written that way for something else — a constant beside the type —
     * would be the type's own name.
     */
    public boolean getNameIsUpperSnake() {
        return TemplateTool.isUpperSnake(getName());
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
        // This unit declares this type, so from inside it the type is written bare: the one
        // case where the two spellings follow from each other with nothing more to know.
        final var name = dsmEnumeration.typeName.name;
        final var proxy = dsmEnumeration.typeName.nameSpace.name + "_" + name;
        return new TemplateBindingType(proxy, typeSuffix, proxy, name, true, true);
    }
}