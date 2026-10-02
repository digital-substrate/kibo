package com.digitalsubstrate.template;

import org.stringtemplate.v4.AttributeRenderer;

import java.util.Locale;

public final class TemplateStringRenderer implements AttributeRenderer<String> {
    @Override
    public String toString(String s, String formatString, Locale locale) {
        if (formatString == null)
            return s;

        return switch (formatString) {
            case "u" -> TemplateTool.u(s);
            case "l" -> TemplateTool.l(s);
            case "uf" -> TemplateTool.uf(s);
            case "lf" -> TemplateTool.lf(s);
            case "usc" -> TemplateTool.usc(s);
            case "lsc" -> TemplateTool.lsc(s);
            case "sc" -> TemplateTool.sc(s);
            case "snake" -> TemplateTool.snake(s);
            case "usnake" -> TemplateTool.usnake(s);
            case "string" -> TemplateTool.string(s);
            case "docstring" -> TemplateTool.docstring(s);
            case "comment" -> TemplateTool.comment(s);
            default -> s;
        };

    }
}

