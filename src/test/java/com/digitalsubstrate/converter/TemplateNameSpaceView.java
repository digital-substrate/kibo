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

    private static String render(java.util.List<TemplateNameSpace> nameSpaces) {
        return nameSpaces.stream()
                .map(TemplateNameSpace::getName)
                .sorted()
                .collect(Collectors.joining(", ", "[", "]"));
    }

    String dependenciesOf(String name) {
        return render(get(name).dependencies.all);
    }

    String typeDependenciesOf(String name) {
        return render(get(name).dependencies.types);
    }

    String attachmentDependenciesOf(String name) {
        return render(get(name).dependencies.attachments);
    }

    int positionOf(String name) {
        return nameSpaces.indexOf(get(name));
    }
}
