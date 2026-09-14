package com.digitalsubstrate.viper.dsm;

import com.digitalsubstrate.viper.Graph;
import com.digitalsubstrate.viper.GraphMap;
import com.digitalsubstrate.viper.NameSpace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class DSMNameSpaceDependency {

    private final HashMap<NameSpace, HashSet<NameSpace>> dependencyByNameSpace = new HashMap<>();
    private final HashMap<NameSpace, HashSet<NameSpace>> typeDependencyByNameSpace = new HashMap<>();
    private final HashMap<NameSpace, HashSet<NameSpace>> attachmentDependencyByNameSpace = new HashMap<>();

    /**
     * Add to {@code dependencies} every namespace {@code type} names, other than the
     * global one and {@code nameSpace} itself.
     *
     * <p>{@code nameSpace} is the scope the declaration is being read from, and may be
     * {@code null} — a pool is declared in no namespace, so it subtracts none and every
     * namespace its signatures name is a dependency.
     */
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

    void collectAttachment(NameSpace nameSpace, DSMAttachment attachment, HashSet<NameSpace> dependencies) {
        collectType(nameSpace, attachment.keyType, dependencies);
        collectType(nameSpace, attachment.documentType, dependencies);
    }

    void collectPrototype(NameSpace nameSpace, DSMFunctionPrototype prototype, HashSet<NameSpace> dependencies) {
        collectType(nameSpace, prototype.returnType, dependencies);
        for (var parameter : prototype.parameters)
            collectType(nameSpace, parameter.type, dependencies);
    }

    // MARK: - Pools
    /**
     * What a pool's signatures reach.
     *
     * <p>A pool holds only functions and belongs to no namespace — the model gives it a
     * uuid and a display name, nothing more — so there is nothing to exclude and no
     * enclosing scope to subtract: every namespace a parameter or a return type names is
     * a dependency, and a pool naming none has none.
     *
     * <p>This is the same question the graph above answers for a namespace, asked of the
     * one kind of declaration a namespace never holds. Without it a pool template has only
     * the whole model to include, which on a per-unit layout is an artefact nothing
     * produces.
     *
     * <p>Computed on demand rather than collected: no pool depends on a pool, so there is
     * no order to establish and nothing for the sort below to carry.
     */
    public HashSet<NameSpace> dependencies(DSMFunctionPool pool) {
        final var dependencies = new HashSet<NameSpace>();
        for (var function : pool.functions)
            collectPrototype(null, function.prototype, dependencies);
        return dependencies;
    }

    public HashSet<NameSpace> dependencies(DSMAttachmentFunctionPool pool) {
        final var dependencies = new HashSet<NameSpace>();
        for (var function : pool.functions)
            collectPrototype(null, function.prototype, dependencies);
        return dependencies;
    }

    // MARK: - Dependency
    /**
     * What each namespace reaches, collected separately by the kind of declaration that
     * reaches it.
     *
     * <p>A namespace's dependencies are not one set. The types it declares reach other
     * namespaces through a concept's parent, a club's members and a structure's fields;
     * its attachments reach them through a key or a document type. An artefact needs the
     * dependencies of <em>what it emits</em>, and emitting the types while including what
     * only the attachments reach is a wrong include that compiles — the worst kind, since
     * nothing reports it.
     *
     * <p>The union is kept as well, because the emission order is a property of the
     * namespace and not of any one artefact.
     */
    public void collect(DSMDefinitionsInspector inspector) {
        for (var nameSpace : inspector.getNameSpaces()) {
            final var byTypes = new HashSet<NameSpace>();
            final var byAttachments = new HashSet<NameSpace>();

            for (var concept : inspector.getDefinitions().concepts)
                if (concept.typeName.nameSpace.equals(nameSpace))
                    collectConcept(nameSpace, concept, byTypes);

            for (var club : inspector.getDefinitions().clubs)
                if (club.typeName.nameSpace.equals(nameSpace))
                    collectClub(nameSpace, club, byTypes);

            for (var structure : inspector.getDefinitions().structures)
                if (structure.typeName.nameSpace.equals(nameSpace))
                    collectStructure(nameSpace, structure, byTypes);

            for (var attachment : inspector.getDefinitions().attachments)
                if (attachment.typeName.nameSpace.equals(nameSpace))
                    collectAttachment(nameSpace, attachment, byAttachments);

            final var all = new HashSet<>(byTypes);
            all.addAll(byAttachments);

            typeDependencyByNameSpace.put(nameSpace, byTypes);
            attachmentDependencyByNameSpace.put(nameSpace, byAttachments);
            dependencyByNameSpace.put(nameSpace, all);
        }
    }

    /** What the types of {@code nameSpace} reach: concept parents, club members, structure fields. */
    public HashSet<NameSpace> typeDependencies(NameSpace nameSpace) {
        return typeDependencyByNameSpace.getOrDefault(nameSpace, new HashSet<>());
    }

    /** What the attachments of {@code nameSpace} reach: their key and document types. */
    public HashSet<NameSpace> attachmentDependencies(NameSpace nameSpace) {
        return attachmentDependencyByNameSpace.getOrDefault(nameSpace, new HashSet<>());
    }

    public HashSet<NameSpace> dependencies(NameSpace nameSpace) {
        return dependencyByNameSpace.getOrDefault(nameSpace, new HashSet<>());
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
