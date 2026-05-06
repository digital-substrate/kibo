package com.digitalsubstrate.kibo;

import com.beust.jcommander.Parameter;

import java.nio.file.Path;

class Options {
    @Parameter(names = {"--help", "-h"}, description = "Show this help message.", help = true)
    boolean help;

    @Parameter(names = {"--version", "-v"}, description = "Show version information.", help = true)
    boolean version;

    @Parameter(names = {"--quiet", "-q"}, description = "Disable output messages.")
    boolean quiet = false;

    @Parameter(names = {"--log", "-l"}, description = "Display internal process steps.")
    boolean log = false;

    @Parameter(names = {"--namespace", "-n"}, description = "an identifier for code encapsulation.", required = true)
    String namespace;

    @Parameter(names = {"--converter", "-c"}, description = "Specify the converter: cpp|python.", required = true)
    String converter;

    @Parameter(names = {"--dsm", "-d"}, description = "DSM Definitions *.dsm.json.", required = true)
    Path definitions;

    @Parameter(names = {"--template", "-t"}, description = "Template directory or file.", required = true)
    Path template;

    @Parameter(names = {"--output", "-o"}, description = "Output directory or file.", required = true)
    Path output;
}
