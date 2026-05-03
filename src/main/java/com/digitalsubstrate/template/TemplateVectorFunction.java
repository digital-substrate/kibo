package com.digitalsubstrate.template;

public final class TemplateVectorFunction {

    private final String type;
    private final String typeSuffix;
    private final String elementTypeSuffix;
    private final String dsmType;
    private final TemplatePythonType pythonType;
    private final TemplatePythonType pythonElementType;

    public TemplateVectorFunction(String type, String typeSuffix, String elementTypeSuffix,
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

    // Python
    public TemplatePythonType getPythonType() {
        return pythonType;
    }

    public TemplatePythonType getPythonElementType() {
        return pythonElementType;
    }
}
