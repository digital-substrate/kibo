package com.digitalsubstrate.template;

import java.util.ArrayList;

public final class TemplateMatFunction {

    private final String type;
    private final long columns;
    private final long rows;
    private final String typeSuffix;
    private final String elementTypeSuffix;
    private final String dsmType;
    private final TemplatePythonType pythonType;
    private final TemplatePythonType pythonElementType;

    public TemplateMatFunction(String type, long columns, long rows, String typeSuffix, String elementTypeSuffix,
                               String dsmType,
                               TemplatePythonType pythonType, TemplatePythonType pythonElementType) {
        this.type = type;
        this.columns = columns;
        this.rows = rows;
        this.typeSuffix = typeSuffix;
        this.dsmType = dsmType;
        this.elementTypeSuffix = elementTypeSuffix;
        this.pythonType = pythonType;
        this.pythonElementType = pythonElementType;
    }

    // DSM
    public String getDsmType() {
        return dsmType;
    }

    public String getColumns() {
        return String.valueOf(columns);
    }

    public String getRows() {
        return String.valueOf(rows);
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
        return "TypeMat";
    }

    public String getViperValue() {
        return "ValueMat";
    }

    // Python
    public TemplatePythonType getPythonType() {
        return pythonType;
    }

    public TemplatePythonType getPythonElementType() {
        return pythonElementType;
    }

    public String getPythonTupleType() {
        final var elements = new ArrayList<String>();
        for (var i = 0; i < columns; i++)
            elements.add(getPythonColumnType());
        return String.format("tuple[%s]", String.join(", ", elements));
    }

    public String getPythonColumnType() {
        var elements = new ArrayList<String>();
        for (var i = 0; i < rows; i++)
            elements.add(pythonElementType.getType());
        return String.format("tuple[%s]", String.join(", ", elements));
    }
}
