package com.digitalsubstrate.template;

import java.util.ArrayList;

public final class TemplateVecFunction {

    private final String type;
    private final long size;
    private final String typeSuffix;
    private final String elementTypeSuffix;
    private final String dsmType;
    private final TemplatePythonType pythonType;
    private final TemplatePythonType pythonElementType;

    public TemplateVecFunction(String type, long size, String typeSuffix, String elementTypeSuffix,
                               String dsmType,
                               TemplatePythonType pythonType, TemplatePythonType pythonElementType) {
        this.type = type;
        this.size = size;
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

    // Python
    public TemplatePythonType getPythonType() {
        return pythonType;
    }

    public TemplatePythonType getPythonElementType() {
        return pythonElementType;
    }

    public String getPythonTupleType() {
        var result = new ArrayList<String>();
        for (var i = 0; i < size; i++)
            result.add(pythonElementType.getType());
        return String.format("tuple[%s]", String.join(", ", result));
    }
}
