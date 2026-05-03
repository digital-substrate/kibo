package com.digitalsubstrate.template;

public final class TemplateTool {

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
}
