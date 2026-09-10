package com.digitalsubstrate.template;

/**
 * One member of a tuple or a variant, carried in all three spaces: what the model calls
 * it, how the native target writes it, and how it appears through a binding.
 */
public final class TemplateType {

    private final String dsmType;
    private final String type;
    private final String typeSuffix;
    private final TemplateBindingType bindingType;

    public TemplateType(String dsmType, String type, String typeSuffix, TemplateBindingType bindingType) {
        this.dsmType = dsmType;
        this.type = type;
        this.typeSuffix = typeSuffix;
        this.bindingType = bindingType;
    }

    public String getDsmType() {
        return dsmType;
    }

    public String getType() {
        return type;
    }

    public String getTypeSuffix() {
        return typeSuffix;
    }

    public TemplateBindingType getBindingType() {
        return bindingType;
    }
}
