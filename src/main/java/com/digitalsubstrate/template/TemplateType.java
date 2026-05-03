package com.digitalsubstrate.template;

public final class TemplateType {

    private final String type;
    private final String typeSuffix;

    public TemplateType(String type, String typeSuffix) {
        this.type = type;
        this.typeSuffix = typeSuffix;
    }

    public String getType() {
        return type;
    }

    public String getTypeSuffix() {
        return typeSuffix;
    }
}
