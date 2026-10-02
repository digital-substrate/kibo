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

    /** The projection of this run: the built-in one, or the project's (its atoms and renames). */
    private static SnakeCase naming = SnakeCase.standard();

    public static void setNaming(SnakeCase snakeCase) {
        naming = snakeCase;
    }

    /**
     * The snake_case name of a static symbol in a generated package — see {@link SnakeCase}.
     *
     * <p>A static name travels nowhere, so it follows its own rule rather than {@link #lsc},
     * which must stay identical to what the runtime computes for {@code Definitions.inject()}.
     */
    public static String snake(String s) {
        return naming.of(s);
    }

    /** {@link #snake} uppercased: an enumeration member, a module-level constant. */
    public static String usnake(String s) {
        return naming.upper(s);
    }

    /**
     * Text as the body of a double-quoted string literal, valid in C++, TypeScript and Python.
     *
     * <p>A model's documentation can span lines and hold quotes; written raw into a literal,
     * it ends the literal early and the generated file no longer compiles.
     */
    public static String string(String s) {
        StringBuilder out = new StringBuilder(s.length());
        for (char c : s.toCharArray()) {
            switch (c) {
                case '\\' -> out.append("\\\\");
                case '"' -> out.append("\\\"");
                case '\n' -> out.append("\\n");
                case '\r' -> out.append("\\r");
                case '\t' -> out.append("\\t");
                default -> out.append(c);
            }
        }
        return out.toString();
    }

    /**
     * Text as the body of a Python triple-quoted docstring: its lines stay lines, and only what
     * would end the docstring or start an escape is escaped.
     */
    public static String docstring(String s) {
        String escaped = s.replace("\\", "\\\\").replace("\"\"\"", "\\\"\\\"\\\"");
        return escaped.endsWith("\"") ? escaped.substring(0, escaped.length() - 1) + "\\\"" : escaped;
    }

    /** Text inside a {@code /** ... *}{@code /} block comment, which it must not close early. */
    public static String comment(String s) {
        return s.replace("*/", "* /");
    }
}
