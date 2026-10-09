package com.digitalsubstrate.template;

public final class TemplateField {

    private final TemplateFieldType type;
    private final String keyType;
    private final String keyTypeInNamespace;
    private final String keyTypeSuffix;
    private final String elementType;
    private final String elementTypeInNamespace;
    private final String elementTypeSuffix;
    private final String elementTypeViperValue;
    private final TemplateBindingType bindingKeyType;
    private final TemplateBindingType bindingElementType;
    private final TemplateBindingType bindingType;
    private final TemplateBindingType bindingKeySetType;
    private final String passBy;
    private String keySetType = "<None>";

    public TemplateField(TemplateFieldType type,
                         String keyType,
                         String keyTypeInNamespace,
                         String keyTypeSuffix,
                         String elementType,
                         String elementTypeInNamespace,
                         String elementTypeSuffix,
                         String elementTypeViperValue,
                         TemplateBindingType bindingKeyType,
                         TemplateBindingType bindingElementType,
                         String passBy) {
        this(type, keyType, keyTypeInNamespace, keyTypeSuffix, elementType, elementTypeInNamespace, elementTypeSuffix,
             elementTypeViperValue, bindingKeyType, bindingElementType, passBy, null, null);
    }

    public TemplateField(TemplateFieldType type,
                         String keyType,
                         String keyTypeInNamespace,
                         String keyTypeSuffix,
                         String elementType,
                         String elementTypeInNamespace,
                         String elementTypeSuffix,
                         String elementTypeViperValue,
                         TemplateBindingType bindingKeyType,
                         TemplateBindingType bindingElementType,
                         String passBy,
                         TemplateBindingType bindingType,
                         TemplateBindingType bindingKeySetType) {
        this.bindingType = bindingType;
        this.bindingKeySetType = bindingKeySetType;
        this.type = type;
        this.keyType = keyType;
        this.keyTypeInNamespace = keyTypeInNamespace;
        this.keyTypeSuffix = keyTypeSuffix;
        this.elementType = elementType;
        this.elementTypeInNamespace = elementTypeInNamespace;
        this.elementTypeSuffix = elementTypeSuffix;
        this.elementTypeViperValue = elementTypeViperValue;
        this.bindingKeyType = bindingKeyType;
        this.bindingElementType = bindingElementType;
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

    /**
     * The C++ type of a set of this map's keys, as a map field's {@code subtract} takes it: the
     * set orders its keys as the map does ({@code Viper::StaticLess} for a key holding a
     * floating-point value).
     */
    public String getKeySetType() {
        return keySetType;
    }

    public TemplateField withKeySetType(String keySetType) {
        this.keySetType = keySetType;
        return this;
    }

    /**
     * The key type as written inside the namespace that owns this field -- a structure's for a
     * structure field, an attachment's for a document -- the same rule as the owner's
     * {@code typeInNamespace}. Qualified {@link #getKeyType()} names can be shadowed there: a
     * concept named like its namespace makes {@code Graph::X} name the concept.
     */
    public String getKeyTypeInNamespace() {
        return keyTypeInNamespace;
    }

    public String getKeyTypeSuffix() {
        return keyTypeSuffix;
    }

    public String getElementType() {
        return elementType;
    }

    /** The element type as written inside the owning namespace; see {@link #getKeyTypeInNamespace()}. */
    public String getElementTypeInNamespace() {
        return elementTypeInNamespace;
    }

    public String getElementTypeSuffix() {
        return elementTypeSuffix;
    }

    public String getElementTypeViperValue() {
        return elementTypeViperValue;
    }

    // Binding
    public TemplateBindingType getBindingKeyType() {
        return bindingKeyType;
    }

    public TemplateBindingType getBindingElementType() {
        return bindingElementType;
    }

    /** The container itself, seen through the binding: the generated class of its shape. */
    public TemplateBindingType getBindingType() {
        return bindingType;
    }

    /**
     * For a map, the set of its keys, seen through the binding: what removing entries by key
     * takes. Absent for any other container.
     */
    public TemplateBindingType getBindingKeySetType() {
        return bindingKeySetType;
    }
}
