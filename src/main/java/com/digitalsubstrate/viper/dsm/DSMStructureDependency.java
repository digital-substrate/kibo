// Copyright (c) Digital Substrate 2026, All rights reserved.

package com.digitalsubstrate.viper.dsm;

import com.digitalsubstrate.viper.Graph;
import com.digitalsubstrate.viper.GraphMap;
import com.digitalsubstrate.viper.TypeName;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;

public final class DSMStructureDependency {

    private final DSMDefinitionsInspector inspector;
    private final HashMap<TypeName, HashSet<TypeName>> dependencyByStructure = new HashMap<>();

    public DSMStructureDependency(DSMDefinitionsInspector inspector) {
        this.inspector = inspector;
        collect();
    }

    private void collect() {
        for (var structure : inspector.getDefinitions().structures) {
            final var dependencies = new HashSet<TypeName>();
            dependencyByStructure.put(structure.typeName, dependencies);
            for (var field : structure.fields) {
                collect(field.type, dependencies);
            }
        }
    }

    private void collect(DSMType type, HashSet<TypeName> dependencies) {
        if (type instanceof DSMTypeTuple typeTuple) {
            for (var elementType : typeTuple.types)
                collect(elementType, dependencies);

        } else if (type instanceof DSMTypeOptional typeOptional) {
            collect(typeOptional.elementType, dependencies);

        } else if (type instanceof DSMTypeVector typeVector) {
            collect(typeVector.elementType, dependencies);

        } else if (type instanceof DSMTypeSet typeSet) {
            collect(typeSet.elementType, dependencies);

        } else if (type instanceof DSMTypeMap typeMap) {
            collect(typeMap.keyType, dependencies);
            collect(typeMap.elementType, dependencies);

        } else if (type instanceof DSMTypeXArray typeXArray) {
            collect(typeXArray.elementType, dependencies);

        } else if (type instanceof DSMTypeVariant typeVariant) {
            for (var elementType : typeVariant.types)
                collect(elementType, dependencies);

        } else if (type instanceof DSMTypeReference typeReference) {
            if (inspector.getStructures().containsKey(typeReference.typeName)) {
                dependencies.add(typeReference.typeName);
            }
        }
    }

    public void debug() {
        for (var structure : dependencyByStructure.keySet()) {
            final var deps = dependencyByStructure.get(structure);
            System.out.printf("%s <- { ", structure);
            for (var dep : deps) {
                System.out.print(dep);
                System.out.print(" ");
            }
            System.out.print("} (");
            System.out.println(")");
        }
    }

    public ArrayList<DSMStructure> sorted() {
        final var graphMap = new GraphMap<TypeName>();
        final ArrayList<TypeName> typeNames = new ArrayList<>();
        for (var structure : inspector.getDefinitions().structures) {
            typeNames.add(structure.typeName);
            graphMap.insert(structure.typeName);
        }

        var graph = new Graph(typeNames.size());
        for (var typeName : typeNames) {
            final var v = graphMap.vertex(typeName);
            for (var dependency : dependencyByStructure.get(typeName)) {
                final var dsmStructure = inspector.getStructures().get(dependency);
                assert (dsmStructure != null);
                final var dv = graphMap.vertex(dependency);
                graph.addEdge(dv, v);
            }
        }
        final var sortedVertices = graph.topologicalSort();
        final var result = new ArrayList<DSMStructure>();
        final var withDependencies = new ArrayList<DSMStructure>();
        final var withoutDependencies = new ArrayList<DSMStructure>();

        for (var sortedVertex : sortedVertices) {
            final var typeName = graphMap.value(sortedVertex);
            final var dsmStructure = inspector.getStructures().get(typeName);

            final var dependencies = dependencyByStructure.get(typeName);
            if (!dependencies.isEmpty()) {
               withDependencies.add(dsmStructure);
            } else {
                withoutDependencies.add(dsmStructure);
            }
        }
        withoutDependencies.sort(Comparator.comparing(DSMStructure::getRepresentation));
        result.addAll(withoutDependencies);
        result.addAll(withDependencies);

        return result;
    }

    public HashSet<TypeName> dependencies(TypeName typeName) {
        return dependencyByStructure.get(typeName);
    }
}
