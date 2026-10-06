package com.digitalsubstrate.template;

import com.digitalsubstrate.viper.dsm.DSMLiteralDomain;
import com.digitalsubstrate.viper.dsm.DSMLiteralValue;

import com.digitalsubstrate.viper.dsm.DSMStructureField;

public final class TemplateStructureField {

    private final DSMStructureField dsmStructureField;

    private final String type;
    private final String typeInNamespace;
    private final String passBy;
    private final boolean isMovable;
    private final String defaultValue;
    private final String typeSuffix;
    private final boolean isTypeAny;
    private final String viperValue;
    private final TemplateBindingType bindingType;
    private final TemplateField field;

    public TemplateStructureField(DSMStructureField dsmStructureField,
                                  String type,
                                  String typeInNamespace,
                                  String passBy,
                                  boolean isMovable,
                                  String defaultValue,
                                  String typeSuffix,
                                  boolean isTypeAny,
                                  String viperValue,
                                  TemplateBindingType bindingType,
                                  TemplateField field) {

        this.dsmStructureField = dsmStructureField;

        this.type = type;
        this.typeInNamespace = typeInNamespace;
        this.passBy = passBy;
        this.isMovable = isMovable;
        this.defaultValue = defaultValue;
        this.typeSuffix = typeSuffix;
        this.isTypeAny = isTypeAny;
        this.viperValue = viperValue;
        this.bindingType = bindingType;
        this.field = field;
    }

    // DSM
    public DSMStructureField getDsmField() {
        return dsmStructureField;
    }

    /** The name as this target spells it: the DSM name, unless the project spells it otherwise. */
    public String getName() {
        return TemplateTool.spell(dsmStructureField.name);
    }

    /** The DSM name, the one the runtime knows: write it where a name is sent to the runtime. */
    public String getDsmName() {
        return dsmStructureField.name;
    }

    public String getPassBy() {
        return passBy;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    /**
     * Whether the model declares a default value for this field. A field that declares none
     * still has a {@code defaultValue} -- {@code {}}, the type's own -- so a template telling
     * the two apart needs this.
     */
    public boolean getHasDefaultValue() {
        return !(dsmStructureField.defaultValue == null
                 || dsmStructureField.defaultValue instanceof DSMLiteralValue literal
                    && literal.domain == DSMLiteralDomain.NONE);
    }

    // Predicates
    public boolean getIsMovable() {
        return isMovable;
    }

    public boolean getIsTypeAny() {
        return isTypeAny;
    }

    // Documentation
    public Boolean getHasDocumentation() {
        return !dsmStructureField.documentation.isEmpty();
    }

    public String getDocumentation() {
        return dsmStructureField.documentation;
    }

    // Type
    public String getType() {
        return type;
    }

    public String getTypeInNamespace() {
        return typeInNamespace;
    }

    public String getTypeSuffix() {
        return typeSuffix;
    }

    // Field
    public TemplateField getField() {
        return field;
    }

    // Viper
    public String getViperValue() {
        return viperValue;
    }

    // Binding
    public TemplateBindingType getBindingType() {
        return bindingType;
    }
}
