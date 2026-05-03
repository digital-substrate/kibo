package com.digitalsubstrate.template;

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
    private final TemplatePythonType pythonType;
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
                                  TemplatePythonType pythonType,
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
        this.pythonType = pythonType;
        this.field = field;
    }

    // DSM
    public DSMStructureField getDsmField() {
        return dsmStructureField;
    }

    public String getName() {
        return dsmStructureField.name;
    }

    public String getPassBy() {
        return passBy;
    }

    public String getDefaultValue() {
        return defaultValue;
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

    // Python
    public TemplatePythonType getPythonType() {
        return pythonType;
    }
}
