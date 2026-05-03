package com.digitalsubstrate.template;

public final class TemplateField {

    private final TemplateFieldType type;
    private final String keyType;
    private final String keyTypeSuffix;
    private final String elementType;
    private final String elementTypeSuffix;
    private final String elementTypeViperValue;
    private final TemplatePythonType pythonKeyType;
    private final TemplatePythonType pythonElementType;
    private final String passBy;

    public TemplateField(TemplateFieldType type,
                         String keyType,
                         String keyTypeSuffix,
                         String elementType,
                         String elementTypeSuffix,
                         String elementTypeViperValue,
                         TemplatePythonType pythonKeyType,
                         TemplatePythonType pythonElementType,
                         String passBy) {
        this.type = type;
        this.keyType = keyType;
        this.keyTypeSuffix = keyTypeSuffix;
        this.elementType = elementType;
        this.elementTypeSuffix = elementTypeSuffix;
        this.elementTypeViperValue = elementTypeViperValue;
        this.pythonKeyType = pythonKeyType;
        this.pythonElementType = pythonElementType;
        this.passBy = passBy;
    }

    public String getPassBy() {
        return passBy;
    }

    // Predicates
    public Boolean getIsNotBox() {
        return type != TemplateFieldType.BOX;
    }

    public Boolean getIsBox() {
        return type == TemplateFieldType.BOX;
    }

    public Boolean getIsSet() {
        return type == TemplateFieldType.SET;
    }

    public Boolean getIsMap() {
        return type == TemplateFieldType.MAP;
    }

    public Boolean getIsXArray() {
        return type == TemplateFieldType.XARRAY;
    }

    // Type
    public String getType() {
        return type.toString();
    }

    public String getKeyType() {
        return keyType;
    }

    public String getKeyTypeSuffix() {
        return keyTypeSuffix;
    }

    public String getElementType() {
        return elementType;
    }

    public String getElementTypeSuffix() {
        return elementTypeSuffix;
    }

    public String getElementTypeViperValue() {
        return elementTypeViperValue;
    }

    // Python
    public TemplatePythonType getPythonKeyType() {
        return pythonKeyType;
    }

    public TemplatePythonType getPythonElementType() {
        return pythonElementType;
    }
}
