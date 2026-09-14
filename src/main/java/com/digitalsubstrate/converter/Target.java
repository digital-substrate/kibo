package com.digitalsubstrate.converter;

/**
 * What kibo is generating for.
 *
 * <p>A target is a pair: how its generated code reaches the runtime ({@link Binding}), and
 * — when it reaches it through a binding — how that binding spells the types crossing it
 * ({@link BindingVocabulary}). The pair is the axis, not the language: a language could in
 * principle appear on both sides, natively and through a binding.
 *
 * <p>The native side is C++ and only C++. Its type spellings are built recursively in the
 * converter rather than looked up, so it carries no vocabulary.
 */
public enum Target {

    CPP("cpp", Binding.NATIVE, null, new CppLayout()),
    PYTHON("python", Binding.DELEGATING, new PythonVocabulary(), new PackageLayout("python")),
    TYPESCRIPT("typescript", Binding.DELEGATING, new TypeScriptVocabulary(), new PackageLayout("typescript"));

    /** The value of {@code --converter}. */
    public final String identifier;

    final Binding binding;
    final BindingVocabulary vocabulary;

    /** Where this target's artefacts go, and how one reaches another. */
    public final TargetLayout layout;

    Target(String identifier, Binding binding, BindingVocabulary vocabulary, TargetLayout layout) {
        this.identifier = identifier;
        this.binding = binding;
        this.vocabulary = vocabulary;
        this.layout = layout;
    }

    public static Target of(String identifier) {
        for (var target : values())
            if (target.identifier.equals(identifier))
                return target;

        return null;
    }
}
