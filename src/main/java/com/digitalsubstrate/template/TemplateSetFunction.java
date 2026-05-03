package com.digitalsubstrate.template;

public final class TemplateSetFunction {

    private final String type;
    private final String typeSuffix;
    private final String elementTypeSuffix;
    private final String dsmType;
    private final TemplatePythonType pythonType;
    private final TemplatePythonType pythonElementType;

    public TemplateSetFunction(String type, String typeSuffix, String elementTypeSuffix,
                               String dsmType,
                               TemplatePythonType pythonType, TemplatePythonType pythonElementType) {
        this.type = type;
        this.typeSuffix = typeSuffix;
        this.elementTypeSuffix = elementTypeSuffix;
        this.dsmType = dsmType;
        this.pythonType = pythonType;
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

    public String getElementTypeSuffix() {
        return elementTypeSuffix;
    }

    // Viper
    public String getViperType() {
        return "TypeSet";
    }

    public String getViperValue() {
       return "ValueSet";
    }

    // Python
    public TemplatePythonType getPythonType() {
        return pythonType;
    }

    public TemplatePythonType getPythonElementType() {
        return pythonElementType;
    }
}
