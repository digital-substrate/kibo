package com.digitalsubstrate.template;

public final class TemplateOptionalFunction {

    private final String type;
    private final String typeSuffix;
    private final String elementType;
    private final String elementTypeSuffix;
    private final String dsmType;
    private final TemplatePythonType pythonType;
    private final TemplatePythonType pythonElementType;

    public TemplateOptionalFunction(String type, String typeSuffix, String elementType, String elementTypeSuffix,
                                    String dsmType,
                                    TemplatePythonType pythonType, TemplatePythonType pythonElementType) {
        this.type = type;
        this.typeSuffix = typeSuffix;
        this.elementType = elementType;
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

    public String getElementType() {
        return elementType;
    }

    public String getElementTypeSuffix() {
        return elementTypeSuffix;
    }

    // Viper
    public String getViperType() {
        return "TypeOptional";
    }

    public String getViperValue() {
        return "ValueOptional";
    }

    // Python
    public TemplatePythonType getPythonType() {
        return pythonType;
    }

    public TemplatePythonType getPythonElementType() {
        return pythonElementType;
    }
}
