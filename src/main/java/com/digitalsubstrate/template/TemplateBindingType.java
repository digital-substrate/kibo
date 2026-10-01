package com.digitalsubstrate.template;

/**
 * A type as it appears through the binding.
 *
 * <p>{@code proxy} is the generated class name and is the same whatever the binding;
 * {@code type} is how the target writes this type, which is the proxy name when the type
 * needs one and the binding's own spelling of a primitive when it does not.
 * <p>{@code annotation} is the type written in full for a target that annotates —
 * {@code Sequence[Colour]}, {@code MaterialKey | None} — as opposed to {@code type}, which
 * stops at the proxy. The two differ only for containers, and only for a binding that does
 * not emit a class per shape; where they differ, {@code annotation} is what a type checker
 * needs and {@code type} is what it cannot use.
 * <p>{@code isNamed} says whether the type is one a unit declares — an enumeration, a
 * structure, a concept, a club — as opposed to a container built from other types. Both
 * need a proxy under a binding that generates one per shape; only the first has a class
 * under a binding that does not, and a template cannot tell them apart from the name.
 * <p>{@code typeInNamespace} is the same thing written from inside one unit, where a type
 * of that unit needs no qualification and a type of another needs its module —
 * {@code Colour} against {@code modela.Colour}. It equals {@code type} wherever no unit is
 * in scope, which is where the flat spelling is the only one that means anything.
 * <p>{@code qualified} is the annotation written from outside every unit: a type of the unit
 * in scope is qualified by its module too -- {@code model_a.MaterialKey} where
 * {@code annotation} says {@code MaterialKey}. A template needs it where a name the unit
 * declares at module level can hide the type: the attachments of a concept named
 * {@code MaterialKey} are a class of that name, beside the key of {@code Material}.
 * <p>Under a native binding there is no binding space: {@code type} is then absent, and
 * only the neutral members carry.
 */
public final class TemplateBindingType {

    private final String proxy;
    private final String typeSuffix;
    private final String type;
    private final String typeInNamespace;
    private final boolean useProxy;
    private final boolean isNamed;
    private final String annotation;
    private final String qualified;
    private String input;
    private String inputQualified;

    public TemplateBindingType(String proxy, String typeSuffix, String type, String typeInNamespace,
                               boolean useProxy) {
        this(proxy, typeSuffix, type, typeInNamespace, useProxy, false, typeInNamespace);
    }

    public TemplateBindingType(String proxy, String typeSuffix, String type, String typeInNamespace,
                               boolean useProxy, boolean isNamed) {
        this(proxy, typeSuffix, type, typeInNamespace, useProxy, isNamed, typeInNamespace);
    }

    public TemplateBindingType(String proxy, String typeSuffix, String type, String typeInNamespace,
                               boolean useProxy, boolean isNamed, String annotation) {
        this(proxy, typeSuffix, type, typeInNamespace, useProxy, isNamed, annotation, annotation);
    }

    public TemplateBindingType(String proxy, String typeSuffix, String type, String typeInNamespace,
                               boolean useProxy, boolean isNamed, String annotation, String qualified) {
        this.qualified = qualified;
        this.isNamed = isNamed;
        this.annotation = annotation;
        this.proxy = proxy;
        this.typeSuffix = typeSuffix;
        this.type = type;
        this.typeInNamespace = typeInNamespace;
        this.useProxy = useProxy;
    }

    public Boolean getUseProxy() {
        return useProxy;
    }

    public Boolean getIsNamed() {
        return isNamed;
    }

    public String getAnnotation() {
        return annotation;
    }

    public String getQualified() {
        return qualified;
    }

    /**
     * The annotation a write accepts, when it is wider than what a read returns: a read hands
     * back the runtime's own value, a write also takes what the runtime decodes into it.
     */
    public String getInput() {
        return input != null ? input : annotation;
    }

    public String getInputQualified() {
        return inputQualified != null ? inputQualified : qualified;
    }

    public TemplateBindingType withInput(String input, String inputQualified) {
        this.input = input;
        this.inputQualified = inputQualified;
        return this;
    }

    public String getProxy() {
        return proxy;
    }

    public String getTypeSuffix() {
        return typeSuffix;
    }

    public String getType() {
        return type;
    }

    public String getTypeInNamespace() {
        return typeInNamespace;
    }
}
