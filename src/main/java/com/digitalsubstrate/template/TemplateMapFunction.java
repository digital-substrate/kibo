package com.digitalsubstrate.template;

public final class TemplateMapFunction {

    private final String type;
    private final String typeSuffix;
    private final String keyTypeSuffix;
    private final String elementTypeSuffix;
    private final String dsmType;
    private final TemplatePythonType pythonType;
    private final TemplatePythonType pythonKeyType;
    private final TemplatePythonType pythonElementType;

    public TemplateMapFunction(String type, String typeSuffix, String keyTypeSuffix, String elementTypeSuffix,
                               String dsmType,
                               TemplatePythonType pythonType, TemplatePythonType pythonKeyType, TemplatePythonType pythonElementType) {
        this.type = type;
        this.typeSuffix = typeSuffix;
        this.keyTypeSuffix = keyTypeSuffix;
        this.elementTypeSuffix = elementTypeSuffix;
        this.dsmType = dsmType;
        this.pythonType = pythonType;
        this.pythonKeyType = pythonKeyType;
        this.pythonElementType = pythonElementType;
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

    // Python
    public TemplatePythonType getPythonType() {
        return pythonType;
    }

    public TemplatePythonType getPythonKeyType() {
        return pythonKeyType;
    }

    public TemplatePythonType getPythonElementType() {
        return pythonElementType;
    }
}
