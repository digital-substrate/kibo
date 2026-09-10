package com.digitalsubstrate.template;

import java.util.ArrayList;

public final class TemplateMatFunction {

    private final String type;
    private final long columns;
    private final long rows;
    private final String typeSuffix;
    private final String elementTypeSuffix;
    private final String dsmType;
    private final TemplateBindingType bindingType;
    private final TemplateBindingType bindingElementType;
    private final String bindingSequenceType;
    private final String bindingColumnType;

    public TemplateMatFunction(String type, long columns, long rows, String typeSuffix, String elementTypeSuffix,
                               String dsmType,
                               TemplateBindingType bindingType, TemplateBindingType bindingElementType,
                               String bindingSequenceType, String bindingColumnType) {
        this.type = type;
        this.columns = columns;
        this.rows = rows;
        this.typeSuffix = typeSuffix;
        this.dsmType = dsmType;
        this.elementTypeSuffix = elementTypeSuffix;
        this.bindingType = bindingType;
        this.bindingElementType = bindingElementType;
        this.bindingSequenceType = bindingSequenceType;
        this.bindingColumnType = bindingColumnType;
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

    // Binding
    public TemplateBindingType getBindingType() {
        return bindingType;
    }

    public TemplateBindingType getBindingElementType() {
        return bindingElementType;
    }

    /** How the target writes this matrix: a sequence of columns. */
    public String getBindingSequenceType() {
        return bindingSequenceType;
    }

    /** How the target writes one of its columns. */
    public String getBindingColumnType() {
        return bindingColumnType;
    }
}
