package com.digitalsubstrate.template;

public final class TemplateAttachedDocumentType {

    private final String type;
    private final String typeInNamespace;
    private final String typeSuffix;
    private final String viperValue;
    private final TemplateStructure structure;
    private final TemplateBindingType bindingType;
    private final TemplateField field;
    private final boolean useBlobId;

    public TemplateAttachedDocumentType(String type,
                                        String typeInNamespace,
                                        String typeSuffix,
                                        String viperValue,
                                        TemplateBindingType bindingType,
                                        TemplateStructure structure,
                                        TemplateField field,
                                        boolean useBlobId) {
        this.type = type;
        this.typeInNamespace = typeInNamespace;
        this.typeSuffix = typeSuffix;
        this.viperValue = viperValue;
        this.bindingType = bindingType;
        this.structure = structure;
        this.field = field;
        this.useBlobId = useBlobId;
    }

    // Predicates
    public Boolean getIsStructure() {
        return structure != null;
    }

    public boolean getUseBlobId() {
        return useBlobId;
    }

    // Component
    public TemplateStructure getStructure() {
        return structure;
    }

    // Type
    public String getType() {
        return type;
    }

    public String getTypeInNamespace() {
        return typeInNamespace;
    }

    public String getTypeSuffix() {
        return typeSuffix;
    }

    // Field
    public TemplateField getField() {
        return field;
    }

    // Viper
    public String getViperValue() {
        return viperValue;
    }

    // Binding
    public TemplateBindingType getBindingType() {
        return bindingType;
    }
}
