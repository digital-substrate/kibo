package com.digitalsubstrate.template;

/**
 * A type as it appears through the binding.
 *
 * <p>{@code proxy} is the generated class name and is the same whatever the binding;
 * {@code type} is how the target writes this type, which is the proxy name when the type
 * needs one and the binding's own spelling of a primitive when it does not.
 * <p>{@code typeInNamespace} is the same thing written from inside one unit, where a type
 * of that unit needs no qualification and a type of another needs its module —
 * {@code Colour} against {@code modela.Colour}. It equals {@code type} wherever no unit is
 * in scope, which is where the flat spelling is the only one that means anything.
 * <p>Under a native binding there is no binding space: {@code type} is then absent, and
 * only the neutral members carry.
 */
public final class TemplateBindingType {

    private final String proxy;
    private final String typeSuffix;
    private final String type;
    private final String typeInNamespace;
    private final boolean useProxy;

    public TemplateBindingType(String proxy, String typeSuffix, String type, String typeInNamespace,
                               boolean useProxy) {
        this.proxy = proxy;
        this.typeSuffix = typeSuffix;
        this.type = type;
        this.typeInNamespace = typeInNamespace;
        this.useProxy = useProxy;
    }

    public Boolean getUseProxy() {
        return useProxy;
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
