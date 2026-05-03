// Copyright (c) Digital Substrate 2026, All rights reserved.

package com.digitalsubstrate.kibo;

import com.beust.jcommander.JCommander;
import com.digitalsubstrate.viper.dsm.DSMDefinitions;
import com.digitalsubstrate.viper.dsm.DSMDefinitionsDecoder;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
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
            final var result = new DSMDefinitionsDecoder(data);
            return result.decode();
        } catch (Exception e) {
            System.err.println("failed to decode DSM definitions");
            System.exit(1);
        }
        return null;
    }

    public static void generateLog(Options options) {
        if (!options.quiet)
            System.out.printf("Render '%s' for '%s' with '%s' in '%s'%n",
                    options.converter,
                    options.namespace,
                    options.template.toString(),
                    options.output.toString());
    }

    // CPP
    static void generateCpp(String generated, DSMDefinitions dsmDefinitions, Options options) throws Exception {
        generateLog(options);
        AppUtils.generateCpp(generated, dsmDefinitions, options.namespace, options.template, options.output, options.log);
    }

    // Python
    static void generatePython(String generated, DSMDefinitions dsmDefinitions, Options options) throws Exception {
        generateLog(options);
        AppUtils.generatePython(generated, dsmDefinitions, options.namespace, options.template, options.output, options.log);
    }

    // Fatal Error
    static void fatalAvailableGenerator(String generator) {
        if (!generators.contains(generator)) {
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

    static final List<String> generators = List.of("cpp", "python");

    public static void main(String[] argv) throws Exception {

        final var APP = "kibo";
        final var VERSION = "1.2.8";
        final var GENERATOR = APP + "-" + VERSION + ".jar";
        final var options = new Options();
        final var jCommander = JCommander.newBuilder().addObject(options).build();

        jCommander.setProgramName("java -jar" + GENERATOR);
        jCommander.setAcceptUnknownOptions(false);
        jCommander.parse(argv);

        final var generated = "Generated from " + options.definitions.toString() + " by " + GENERATOR;

        if (options.help) {
            jCommander.usage();
            System.exit(0);
        }

        if (options.version) {
            System.out.println(VERSION);
            System.exit(0);
        }

        fatalAvailableGenerator(options.converter);
        fatalPathExists(options.definitions);

        final var data = fatalReadBinaryFile(options.definitions.toString());
        final var definitions = fatalDecodeDefinitions(data);

        switch (options.converter) {
            case "cpp" -> generateCpp(generated, definitions, options);
            case "python" -> generatePython(generated, definitions, options);
            default -> {
            }
        }

        System.exit(0);
    }
}
