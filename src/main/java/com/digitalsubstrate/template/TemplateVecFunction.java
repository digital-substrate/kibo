package com.digitalsubstrate.template;

public final class TemplateVecFunction {

    private final String type;
    private final long size;
    private final String typeSuffix;
    private final String elementTypeSuffix;
    private final String dsmType;
    private final TemplateBindingType bindingType;
    private final TemplateBindingType bindingElementType;
    private final String bindingSequenceType;

    public TemplateVecFunction(String type, long size, String typeSuffix, String elementTypeSuffix,
                               String dsmType,
                               TemplateBindingType bindingType, TemplateBindingType bindingElementType,
                               String bindingSequenceType) {
        this.type = type;
        this.size = size;
        this.typeSuffix = typeSuffix;
        this.elementTypeSuffix = elementTypeSuffix;
        this.dsmType = dsmType;
        this.bindingType = bindingType;
        this.bindingElementType = bindingElementType;
        this.bindingSequenceType = bindingSequenceType;
    }

    // DSM
    public String getDsmType() {
        return dsmType;
    }

    public String getSize() {
        return String.valueOf(size);
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

    // Viper
    public String getViperType() {
        return "TypeVec";
    }

    public String getViperValue() {
        return "ValueVec";
    }

    // Binding
    public TemplateBindingType getBindingType() {
        return bindingType;
    }

    public TemplateBindingType getBindingElementType() {
        return bindingElementType;
    }

    /** How the target writes this fixed-size sequence. */
    public String getBindingSequenceType() {
        return bindingSequenceType;
    }
}
