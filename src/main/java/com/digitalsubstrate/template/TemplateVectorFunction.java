package com.digitalsubstrate.template;

public final class TemplateVectorFunction {

    private final String type;
    private final String typeSuffix;
    private final String elementTypeSuffix;
    private final String dsmType;
    private final TemplateBindingType bindingType;
    private final TemplateBindingType bindingElementType;

    public TemplateVectorFunction(String type, String typeSuffix, String elementTypeSuffix,
                                  String dsmType,
                                  TemplateBindingType bindingType, TemplateBindingType bindingElementType) {
        this.type = type;
        this.typeSuffix = typeSuffix;
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

    public String getElementTypeSuffix() {
        return elementTypeSuffix;
    }

    public String getValueRef() {
        return elementTypeSuffix.equals("_bool") ? "" : "&";
    }

    // Viper
    public String getViperType() {
        return "TypeVector";
    }

    public String getViperValue() {
        return "ValueVector";
    }

    // Binding
    public TemplateBindingType getBindingType() {
        return bindingType;
    }

    public TemplateBindingType getBindingElementType() {
        return bindingElementType;
    }
}
