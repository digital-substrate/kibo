package com.digitalsubstrate.template;

import com.digitalsubstrate.viper.dsm.DSMLexicon;

public final class TemplateTool {

    /**
     * How the untyped concept is spelled in generated C++.
     *
     * <p>The DSM calls it {@code any_concept}, and the usual capitalisation of a model name
     * would give {@code Any_concept} — legal, and read by nobody as a type. The rule was
     * already applied in one place and not the others, so it is named here once.
     */
    public static String typeName(String name) {
        return name.equals(DSMLexicon.AnyConcept) ? "AnyConcept" : uf(name);
    }

    public static String u(String s) {
        return s.toUpperCase();
    }

    public static String l(String s) {
        return s.toLowerCase();
    }

    public static String uf(String s) {
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    public static String lf(String s) {
        return Character.toLowerCase(s.charAt(0)) + s.substring(1);
    }

    public static String sc(String s) {
        if (s.isEmpty())
            return s;

        StringBuilder snake = new StringBuilder();
        for (int i = 0; i < s.length(); ++i) {
            char c = s.charAt(i);
            Character cn = i + 1 < s.length() - 1 ? s.charAt(i + 1) : null;
            Character cp = i > 0 ? s.charAt(i - 1) : null;
            if (Character.isUpperCase(c) || Character.isDigit(c)) {
                boolean nextIsLower = (cn != null && Character.isLowerCase(cn));
                boolean previousIsLower = (cp != null && Character.isLowerCase(cp));
                boolean isPreviousUnderscore = (cp != null && cp == '_');
                if (!snake.isEmpty() && !isPreviousUnderscore && (nextIsLower || previousIsLower))
                    snake.append('_');
            }
            snake.append(c);
        }
        return snake.toString();
    }

    public static String usc(String s) {
        return u(sc(s));
    }

    public static String lsc(String s) {
        return l(sc(s));
    }

    /**
     * The snake_case name of a static symbol: a Python field, method or parameter.
     *
     * <p>A name the model already spells without a capital is kept as written:
     * {@code f_uint8} stays {@code f_uint8}. {@link #lsc} would split it before the digit,
     * which its rule requires for the names that travel -- the attachment pool's functions,
     * the constants of the embedded module -- and which must stay identical to the runtime's.
     * A static name travels nowhere, so it can keep what the author wrote; a name in
     * camelCase still goes through {@link #lsc}, so {@code propertiesInt8} gives
     * {@code properties_int_8}, as the attachment pool spells it.
     */
    public static String snake(String s) {
        return s.chars().anyMatch(Character::isUpperCase) ? lsc(s) : s;
    }
}
