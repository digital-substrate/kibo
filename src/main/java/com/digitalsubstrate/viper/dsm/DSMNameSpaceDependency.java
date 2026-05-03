package com.digitalsubstrate.viper.dsm;

import com.digitalsubstrate.viper.Graph;
import com.digitalsubstrate.viper.GraphMap;
import com.digitalsubstrate.viper.NameSpace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class DSMNameSpaceDependency {

    private final HashMap<NameSpace, HashSet<NameSpace>>  dependencyByNameSpace = new HashMap<>();

    void collectType(NameSpace nameSpace, DSMType type, HashSet<NameSpace> dependencies) {
        if (type instanceof DSMTypeKey typeKey) {
            collectType(nameSpace, typeKey.elementType, dependencies);

        } else if (type instanceof DSMTypeTuple typeTuple) {
            for (var elementType : typeTuple.types)
                collectType(nameSpace, elementType, dependencies);

        } else if (type instanceof DSMTypeOptional typeOptional) {
            collectType(nameSpace, typeOptional.elementType, dependencies);

        } else if (type instanceof DSMTypeVector typeVector) {
            collectType(nameSpace, typeVector.elementType, dependencies);

        } else if (type instanceof DSMTypeSet typeSet) {
            collectType(nameSpace, typeSet.elementType, dependencies);

        } else if (type instanceof DSMTypeMap typeMap) {
            collectType(nameSpace, typeMap.keyType, dependencies);
            collectType(nameSpace, typeMap.elementType, dependencies);

        } else if (type instanceof DSMTypeXArray typeXArray) {
            collectType(nameSpace, typeXArray.elementType, dependencies);

        } else if (type instanceof DSMTypeVariant typeVariant) {
            for (var elementType : typeVariant.types)
                collectType(nameSpace, elementType, dependencies);

        } else if (type instanceof DSMTypeReference typeReference) {
            if (!typeReference.typeName.nameSpace.isGlobal() && !typeReference.typeName.nameSpace.equals(nameSpace)) {
                dependencies.add(typeReference.typeName.nameSpace);
            }
        }
    }

    void collectConcept(NameSpace nameSpace, DSMConcept concept, HashSet<NameSpace> dependencies) {
        if (concept.parent != null)
            collectType(nameSpace, concept.parent, dependencies);
    }

    void collectClub(NameSpace nameSpace, DSMClub club, HashSet<NameSpace> dependencies) {
        for (var member: club.members)
            collectType(nameSpace, member, dependencies);
    }

    void collectStructure(NameSpace nameSpace, DSMStructure structure, HashSet<NameSpace> dependencies) {
        for (var field: structure.fields)
            collectType(nameSpace, field.type, dependencies);
    }

    // MARK: - Dependency
    public void collect(DSMDefinitionsInspector inspector) {
        for (var nameSpace : inspector.getNameSpaces()) {
            final var dependencies = new HashSet<NameSpace>();

            for (var concept : inspector.getDefinitions().concepts)
                if (concept.typeName.nameSpace.equals(nameSpace))
                    collectConcept(nameSpace, concept, dependencies);

            for (var club : inspector.getDefinitions().clubs)
                if (club.typeName.nameSpace.equals(nameSpace))
                    collectClub(nameSpace, club, dependencies);

            for (var structure : inspector.getDefinitions().structures) {
                if (structure.typeName.nameSpace.equals(nameSpace))
                    collectStructure(nameSpace, structure, dependencies);
            }
            dependencyByNameSpace.put(nameSpace, dependencies);
        }
    }

    public void debug() {
        System.out.println("Namespace dependencies:");
        for (Map.Entry<NameSpace, HashSet<NameSpace>> entry : dependencyByNameSpace.entrySet()) {
            final var nameSpace = entry.getKey();
            final var dependencies = entry.getValue();

            final var reprs = new ArrayList<String>();
            for (var dep: dependencies)
                reprs.add(dep.name);
            System.out.format("%s <-- { %s }\n", nameSpace.name, String.join(", ", reprs));
        }
    }

    public ArrayList<NameSpace> sorted() {
        final var graphMap = new GraphMap<NameSpace>();
        for (var nameSpace: dependencyByNameSpace.keySet())
            graphMap.insert(nameSpace);

        final var graph = new Graph(graphMap.size());

        for (Map.Entry<NameSpace, Integer> entry : graphMap.valueToVertex.entrySet()) {
            final var nameSpace = entry.getKey();
            final var vertex = entry.getValue();
            for (var dependency : dependencyByNameSpace.get(nameSpace))
                graph.addEdge(graphMap.vertex(dependency), vertex);
        }

        final var result = new ArrayList<NameSpace>();
        for (var v : graph.topologicalSort())
            result.add(graphMap.value(v));

        return result;
    }
}
