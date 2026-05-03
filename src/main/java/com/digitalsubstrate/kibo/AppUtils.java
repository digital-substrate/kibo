// Copyright (c) Digital Substrate 2026, All rights reserved.

package com.digitalsubstrate.kibo;

import com.digitalsubstrate.converter.Converter;
import com.digitalsubstrate.viper.dsm.DSMDefinitions;
import com.digitalsubstrate.template.TemplateDefinitions;
import com.digitalsubstrate.template.TemplateStringRenderer;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroupFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public final class AppUtils {

    private AppUtils() {
    }

    // Templates
    public static Path outputFilePath(Path template) {
        final var filename = template.getFileName().toString();
        return Path.of(filename.substring(0, filename.lastIndexOf('.')));
    }

    public static ArrayList<Path> collectTemplates(Path template) {
        final var result = new ArrayList<Path>();
        if (Files.isDirectory(template)) {
            final var templateFile = new File(template.toString());
            final var files = templateFile.listFiles();
            if (files != null) {
                for (var file : files) {
                    if (file.getName().endsWith(".stg")) {
                        result.add(file.toPath());
                    }
                }
            }
        } else {
            result.add(template);
        }
        return result;
    }

    public static String render(Path template, Object obj, boolean debug) {
        if (debug) {
            System.out.println("Render " + template);
        }

        final var templates = new STGroupFile(template.toString());
        templates.registerRenderer(String.class, new TemplateStringRenderer());
        final ST main = templates.getInstanceOf("main");
        main.add("m", obj);
        return main.render();
    }

    public static void renderAndSave(String file_prefix, TemplateDefinitions templateDefinitions, Path template, Path output, boolean debug) throws Exception {
        final var code = render(template, templateDefinitions, debug);
        final var filename = file_prefix + outputFilePath(template);
        final var filePath = Paths.get(output.toString(), filename);
        if (debug)
            System.out.printf("Save %s%n", filePath);

        FileUtils.saveSource(code, filePath);
    }

    public static void renderAndSave(String file_prefix, TemplateDefinitions templateDefinitions, ArrayList<Path> templates, Path output, boolean debug) throws Exception {
        Files.createDirectories(output);
        for (var template : templates)
            renderAndSave(file_prefix, templateDefinitions, template, output, debug);
    }

    // Cpp
    public static void generateCpp(String generated, DSMDefinitions dsmDefinitions, String namespace, Path template, Path output, boolean debug) throws Exception {
        final var templateDefinitions = new Converter(generated, dsmDefinitions, namespace).convert();
        final var file_prefix = templateDefinitions.getNamespace() + "_";
        renderAndSave(file_prefix, templateDefinitions, AppUtils.collectTemplates(template), output, debug);
    }

    // Python
    public static void generatePython(String generated, DSMDefinitions dsmDefinitions, String namespace, Path template, Path output, boolean debug) throws Exception {
        final var templateDefinitions = new Converter(generated, dsmDefinitions, namespace).convert();
        renderAndSave("", templateDefinitions, AppUtils.collectTemplates(template), output, debug);
    }
}
