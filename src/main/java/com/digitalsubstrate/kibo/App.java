package com.digitalsubstrate.kibo;

import com.beust.jcommander.JCommander;
import com.digitalsubstrate.converter.Target;
import com.digitalsubstrate.template.NameCollisions;
import com.digitalsubstrate.template.SnakeCase;
import com.digitalsubstrate.template.TemplateTool;
import com.digitalsubstrate.viper.dsm.DSMDefinitions;
import com.digitalsubstrate.viper.dsm.DSMDefinitionsJsonDecoder;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

public final class App {

    // DSMDefinitions
    public static byte[] fatalReadBinaryFile(String path) {
        try {
            final var s = new FileInputStream(path);
            final var d = new DataInputStream(s);

            final var result = d.readAllBytes();

            s.close();
            d.close();
            return result;

        } catch (Exception e) {
            System.err.printf("failed to load DSM definitions file %s.", path);
            System.exit(1);
        }
        return null;
    }

    public static DSMDefinitions fatalDecodeDefinitions(byte[] data) {
        try {
            return new DSMDefinitionsJsonDecoder(data).decode();
        } catch (Exception e) {
            System.err.println("failed to decode DSM definitions: " + e.getMessage());
            System.exit(1);
        }
        return null;
    }

    public static void generateLog(Options options) {
        if (!options.quiet)
            System.out.printf("Render '%s' for '%s' with '%s' in '%s'%n",
                    options.converter,
                    options.namespace,
                    String.join(", ", options.templates),
                    options.output.toString());
    }

    static void generate(Target target, String generated, DSMDefinitions dsmDefinitions, Options options) throws Exception {
        generateLog(options);
        AppUtils.generate(target, generated, dsmDefinitions, options.namespace,
                          options.templates.stream().map(Path::of).toList(), options.output, options.log);
    }

    // Naming
    static SnakeCase fatalNaming(Options options) {
        final var renames = new LinkedHashMap<String, String>();
        for (var rename : options.renames) {
            final var at = rename.indexOf('=');
            if (at <= 0 || at == rename.length() - 1) {
                System.err.printf("--rename %s: expected Name=snake_name.%n", rename);
                System.exit(1);
            }
            renames.put(rename.substring(0, at), rename.substring(at + 1));
        }
        return SnakeCase.of(options.atoms, renames, new java.util.LinkedHashSet<>(options.reserved));
    }

    static void fatalNameCollisions(DSMDefinitions definitions) {
        final var collisions = NameCollisions.find(definitions);
        if (collisions.isEmpty())
            return;
        for (var collision : collisions)
            System.err.println(collision);
        System.exit(1);
    }

    // Fatal Error
    static void fatalAvailableGenerator(String generator) {
        if (Target.of(generator) == null) {
            System.err.printf("%s: No such generator.%n", generator);
            System.exit(1);
        }
    }

    static void fatalPathExists(Path path) {
        if (!Files.exists(path)) {
            System.err.printf("%s: No such file or directory.%n", path);
            System.exit(1);
        }
    }

    static final List<String> generators = Arrays.stream(Target.values()).map(t -> t.identifier).toList();

    public static void main(String[] argv) throws Exception {

        final var APP = "kibo";
        final var VERSION = "2.0.1";
        final var GENERATOR = APP + "-" + VERSION + ".jar";
        final var options = new Options();
        final var jCommander = JCommander.newBuilder().addObject(options).build();

        jCommander.setProgramName("java -jar" + GENERATOR);
        jCommander.setAcceptUnknownOptions(false);
        jCommander.parse(argv);

        if (options.help) {
            jCommander.usage();
            System.exit(0);
        }

        if (options.version) {
            System.out.println(VERSION);
            System.exit(0);
        }

        final var generated = "Generated from " + options.definitions.toString() + " by " + GENERATOR;

        fatalAvailableGenerator(options.converter);
        fatalPathExists(options.definitions);

        final var data = fatalReadBinaryFile(options.definitions.toString());
        final var definitions = fatalDecodeDefinitions(data);

        TemplateTool.setNaming(fatalNaming(options));
        if (options.converter.equals("python") || options.converter.equals("typescript"))
            fatalNameCollisions(definitions);

        switch (options.converter) {
            case "cpp", "python", "typescript" -> generate(Target.of(options.converter), generated, definitions, options);
            default -> {
            }
        }

        System.exit(0);
    }
}
