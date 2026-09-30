package com.digitalsubstrate.template;

import com.digitalsubstrate.viper.TypeName;

public final class TemplateAttachedKeyType {

    private final TypeName typeName;
    private final String type;
    private final String typeInNamespace;
    private final String typeSuffix;
    private final String viperValue;
    private final TemplateBindingType bindingType;
    private final TemplateBindingType bindingKeySetType;

    public TemplateAttachedKeyType(TypeName typeName,
                                   String type,
                                   String typeInNamespace,
                                   String typeSuffix,
                                   String viperValue,
                                   TemplateBindingType bindingType) {
        this(typeName, type, typeInNamespace, typeSuffix, viperValue, bindingType, null);
    }

    public TemplateAttachedKeyType(TypeName typeName,
                                   String type,
                                   String typeInNamespace,
                                   String typeSuffix,
                                   String viperValue,
                                   TemplateBindingType bindingType,
                                   TemplateBindingType bindingKeySetType) {
        this.bindingKeySetType = bindingKeySetType;
        this.typeName = typeName;
        this.type = type;
        this.typeInNamespace = typeInNamespace;
        this.typeSuffix = typeSuffix;
        this.viperValue = viperValue;
        this.bindingType = bindingType;
    }

    // Namespace
    public String getNamespace() {
        return typeName.nameSpace.name;
    }

    public String getName() {
        return TemplateTool.typeName(typeName.name);
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

    // Binding
    /** The set of these keys, seen through the binding: what listing an attachment's keys returns. */
    public TemplateBindingType getBindingKeySetType() {
        return bindingKeySetType;
    }

    public TemplateBindingType getBindingType() {
        return bindingType;
    }
}
