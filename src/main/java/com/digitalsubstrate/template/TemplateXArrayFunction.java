package com.digitalsubstrate.template;

public final class TemplateXArrayFunction {

    private final String type;
    private final String typeSuffix;
    private final String elementType;
    private final String elementTypeSuffix;
    private final String dsmType;
    private final TemplateBindingType bindingType;
    private final TemplateBindingType bindingElementType;

    public TemplateXArrayFunction(String type, String typeSuffix, String elementType, String elementTypeSuffix,
                                  String dsmType,
                                  TemplateBindingType bindingType, TemplateBindingType bindingElementType) {
        this.type = type;
        this.typeSuffix = typeSuffix;
        this.elementType = elementType;
        this.elementTypeSuffix = elementTypeSuffix;
        this.dsmType = dsmType;
        this.bindingType = bindingType;
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

    public String getElementType() {
        return elementType;
    }

    public String getElementTypeSuffix() {
        return elementTypeSuffix;
    }

    // Viper
    public String getViperType() {
        return "TypeXArray";
    }

    public String getViperValue() {
        return "ValueXArray";
    }

    // Binding
    public TemplateBindingType getBindingType() {
        return bindingType;
    }

    public TemplateBindingType getBindingElementType() {
        return bindingElementType;
    }
}
