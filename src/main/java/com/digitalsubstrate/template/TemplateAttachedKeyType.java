package com.digitalsubstrate.template;

import com.digitalsubstrate.viper.TypeName;

public final class TemplateAttachedKeyType {

    private final TypeName typeName;
    private final String type;
    private final String typeInNamespace;
    private final String typeSuffix;
    private final String viperValue;
    private final TemplatePythonType pythonType;

    public TemplateAttachedKeyType(TypeName typeName,
                                   String type,
                                   String typeInNamespace,
                                   String typeSuffix,
                                   String viperValue,
                                   TemplatePythonType pythonType) {
        this.typeName = typeName;
        this.type = type;
        this.typeInNamespace = typeInNamespace;
        this.typeSuffix = typeSuffix;
        this.viperValue = viperValue;
        this.pythonType = pythonType;
    }

    // Namespace
    public String getNamespace() {
        return typeName.nameSpace.name;
    }

    public String getName() {
        return typeName.name.equals("any_concept") ? "AnyConcept" : typeName.name;
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

    // Viper
    public String getViperValue() {
        return viperValue;
    }

    // Python
    public TemplatePythonType getPythonType() {
        return pythonType;
    }
}
