package com.digitalsubstrate.converter;

import com.digitalsubstrate.template.TemplateDefinitions;
import com.digitalsubstrate.template.TemplateNameSpace;

import java.util.ArrayList;
import java.util.stream.Collectors;

/** Reads namespaces out of a converted model, by name, for assertions. */
final class TemplateNameSpaceView {

    private final ArrayList<TemplateNameSpace> nameSpaces;

    TemplateNameSpaceView(TemplateDefinitions definitions) {
        this.nameSpaces = definitions.nameSpaces;
    }

    private TemplateNameSpace get(String name) {
        return nameSpaces.stream()
                .filter(n -> n.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new AssertionError("no namespace named " + name));
    }

    String dependenciesOf(String name) {
        return get(name).dependencies.stream()
                .map(TemplateNameSpace::getName)
                .sorted()
                .collect(Collectors.joining(", ", "[", "]"));
    }

    int positionOf(String name) {
        return nameSpaces.indexOf(get(name));
    }
}
