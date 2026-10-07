package com.digitalsubstrate.template;

import com.digitalsubstrate.viper.dsm.DSMStructure;

import java.util.ArrayList;

public final class TemplateStructure {
    private String qualified;

    private final DSMStructure dsmStructure;
    private final String type;
    private final String typeSuffix;
    private final ArrayList<TemplateStructureField> fields = new ArrayList<>();
    private final boolean isMovable;

    public TemplateStructure(DSMStructure dsmStructure, String type, String typeSuffix, boolean isMovable) {
        this.dsmStructure = dsmStructure;
        this.type = type;
        this.typeSuffix = typeSuffix;
        this.isMovable = isMovable;
    }

    // DSM
    public DSMStructure getDsmStructure() {
        return dsmStructure;
    }

    // Components
    public ArrayList<TemplateStructureField> getFields() {
        return fields;
    }

    // Predicates
    public boolean getIsMovable() {
        return isMovable;
    }

    // Namespace
    public String getNamespace() {
        return TemplateTool.spell(dsmStructure.typeName.nameSpace.name);
    }

    /** The name as this target spells it: the DSM name, unless the project spells it otherwise. */
    public String getName() {
        return TemplateTool.spell(dsmStructure.typeName.name);
    }

    /** The DSM name, the one the runtime knows: write it where a name is sent to the runtime. */
    public String getDsmName() {
        return dsmStructure.typeName.name;
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
        return dsmStructure.runtimeId.toString().toLowerCase();
    }

    // Documentation
    public Boolean getHasDocumentation() {
        return !dsmStructure.documentation.isEmpty();
    }

    public String getDocumentation() {
        return dsmStructure.documentation;
    }

    // Type
    /** The DSM name of this entity — what the model calls it, whatever the target. */
    public String getDsmType() {
        return dsmStructure.typeName.representation();
    }

    public String getType() {
        return type;
    }

    public String getTypeSuffix() {
        return typeSuffix;
    }

    // Viper
    public String getViperType() {
        return "TypeStructure";
    }

    public String getViperValue() {
        return "ValueStructure";
    }

    // Binding
    /**
     * The type as the binding writes it. This unit declares it, so from inside it the type is
     * written bare: the one case where the two spellings follow from each other with nothing
     * more to know. {@code qualified} is the converter's, written from outside every unit and
     * so qualified by its module, as for a field that names the type; a native binding has no
     * binding space and none to give.
     */
    public TemplateBindingType getBindingType() {
        final var name = dsmStructure.typeName.name;
        final var proxy = dsmStructure.typeName.nameSpace.name + "_" + name;
        final var bare = name;
        return new TemplateBindingType(proxy, typeSuffix, proxy, bare, true, true, bare,
                                       qualified != null ? qualified : bare);
    }

    public TemplateStructure withQualified(String qualified) {
        this.qualified = qualified;
        return this;
    }

}
