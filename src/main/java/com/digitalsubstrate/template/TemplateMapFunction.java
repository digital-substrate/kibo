package com.digitalsubstrate.template;

public final class TemplateMapFunction {

    private final String type;
    private final String typeSuffix;
    private final String keyTypeSuffix;
    private final String elementTypeSuffix;
    private final String dsmType;
    private final TemplateBindingType bindingType;
    private final TemplateBindingType bindingKeyType;
    private final TemplateBindingType bindingElementType;

    public TemplateMapFunction(String type, String typeSuffix, String keyTypeSuffix, String elementTypeSuffix,
                               String dsmType,
                               TemplateBindingType bindingType, TemplateBindingType bindingKeyType, TemplateBindingType bindingElementType) {
        this.type = type;
        this.typeSuffix = typeSuffix;
        this.keyTypeSuffix = keyTypeSuffix;
        this.elementTypeSuffix = elementTypeSuffix;
        this.dsmType = dsmType;
        this.bindingType = bindingType;
        this.bindingKeyType = bindingKeyType;
        this.bindingElementType = bindingElementType;
    }

    // DSM
    public String getDsmType() {
        return dsmType;
    }

    // Type
    public String getType() {
        return type;
    }

    public String getTypeSuffix() {
        return typeSuffix;
    }

    public String getKeyTypeSuffix() {
        return keyTypeSuffix;
    }

    public String getElementTypeSuffix() {
        return elementTypeSuffix;
    }

    // Viper
    public String getViperType() {
        return "TypeMap";
    }

    public String getViperValue() {
        return "ValueMap";
    }

    // Binding
    public TemplateBindingType getBindingType() {
        return bindingType;
    }

    public TemplateBindingType getBindingKeyType() {
        return bindingKeyType;
    }

    public TemplateBindingType getBindingElementType() {
        return bindingElementType;
    }
}
