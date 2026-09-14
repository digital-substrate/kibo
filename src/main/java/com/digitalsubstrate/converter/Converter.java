package com.digitalsubstrate.converter;

import com.digitalsubstrate.template.*;
import com.digitalsubstrate.viper.TypeName;
import com.digitalsubstrate.viper.dsm.*;

import java.util.Comparator;
import com.digitalsubstrate.viper.NameSpace;
import java.util.HashMap;

public final class Converter {
    private final DSMStructureDependency structureDependency;
    private final HashMap<TypeName, DSMStructure> structuresByTypeName = new HashMap<>();

    private final HashMap<String, String> cppPrimitiveTypes = new HashMap<>();
    private final HashMap<String, String> viperPrimitiveValues = new HashMap<>();

    private final LiteralConverter literalConverter;
    private final TypeConverter typeConverter;
    private final FunctionRegistrar functionRegistrar;
    private final EntityConverter entityConverter;

    public final String generated;
    public final DSMDefinitions definitions;
    public final DSMDefinitionsInspector inspector;
    public final String namespace;
    public boolean hasTypeAny;

    private final Binding binding;
    private final TargetLayout layout;

    public Converter(String generated, DSMDefinitions definitions, String namespace, Target target) {
        this.binding = target.binding;
        this.layout = target.layout;
        this.generated = generated;
        this.definitions = definitions;
        this.namespace = namespace;
        this.hasTypeAny = false;
        this.inspector = new DSMDefinitionsInspector(definitions);
        this.structureDependency = new DSMStructureDependency(this.inspector);
        this.literalConverter = new LiteralConverter(structuresByTypeName);
        this.typeConverter = new TypeConverter(cppPrimitiveTypes, viperPrimitiveValues, structuresByTypeName, target.vocabulary);
        this.functionRegistrar = new FunctionRegistrar(definitions, typeConverter, binding);
        this.entityConverter = new EntityConverter(definitions, structureDependency, typeConverter, literalConverter, functionRegistrar, binding);

        populateMaps();
        registerPrimitives();
    }

    // Definitions
    public TemplateDefinitions convert() throws Exception {
        final var result = new TemplateDefinitions(generated, namespace, layout);

        result.sortedStructures.addAll(entityConverter.convertStructures());
        result.enumerations.addAll(entityConverter.convertEnumerations());

        final var attachments = entityConverter.convertAttachments(definitions.attachments);
        final var concepts = entityConverter.convertConcepts();

        // Fill template concept isa and members
        for (var concept : concepts) {
            if (concept.getDsmConcept().parent != null) {
                final var parent = entityConverter.templateConceptByTypeName.get(concept.getDsmConcept().parent.typeName);
                concept.setParent(parent);
            }
            final var children = entityConverter.collectConceptChildren(concept.getDsmConcept());
            concept.setChildren(children);
        }

        final var functionPools = entityConverter.convertFunctionPool();
        final var attachmentFunctionPools = entityConverter.convertAttachmentFunctionPool();

        result.concepts.addAll(concepts);
        result.clubs.addAll(entityConverter.convertClubs());

        result.attachments.addAll(attachments);

        result.functionPools.addAll(functionPools);
        result.attachmentFunctionPools.addAll(attachmentFunctionPools);

        if (binding.needsDerivedContainerProxies())
            functionRegistrar.registerFunctionForContainer(new DSMTypeOptional(DSMTypeReference.AnyConcept));

        functionRegistrar.registerFunctionForStructures();
        functionRegistrar.emitContainerFunctions(result);

        result.structures.addAll(result.sortedStructures);
        result.structures.sort(Comparator.comparing(TemplateStructure::getType));
        result.enumerations.sort(Comparator.comparing(TemplateEnumeration::getType));
        result.concepts.sort(Comparator.comparing(TemplateConcept::getType));
        result.clubs.sort(Comparator.comparing(TemplateClub::getType));

        result.vecFunctions.sort(Comparator.comparing(TemplateVecFunction::getType));
        result.matFunctions.sort(Comparator.comparing(TemplateMatFunction::getType));
        result.tupleFunctions.sort(Comparator.comparing(TemplateTupleFunction::getType));
        result.optionalFunctions.sort(Comparator.comparing(TemplateOptionalFunction::getType));
        result.vectorFunctions.sort(Comparator.comparing(TemplateVectorFunction::getType));
        result.setFunctions.sort(Comparator.comparing(TemplateSetFunction::getType));
        result.mapFunctions.sort(Comparator.comparing(TemplateMapFunction::getType));
        result.xarrayFunctions.sort(Comparator.comparing(TemplateXArrayFunction::getType));
        result.variantFunctions.sort(Comparator.comparing(TemplateVariantFunction::getType));

        result.attachments.sort(Comparator.comparing(TemplateAttachment::getIdentifier));

        result.functionPools.sort(Comparator.comparing(TemplateFunctionPool::getName));
        result.attachmentFunctionPools.sort(Comparator.comparing(TemplateAttachmentFunctionPool::getName));

        for (var pool : result.functionPools)
            pool.setModel(result, layout);
        for (var pool : result.attachmentFunctionPools)
            pool.setModel(result, layout);

        // Create NameSpaces
        final var nameSpaceDependency = fillNameSpaces(result);
        fillPoolDependencies(result, nameSpaceDependency);
        return result;
    }

    private static void fill(java.util.ArrayList<TemplateNameSpace> into,
                             java.util.Collection<com.digitalsubstrate.viper.NameSpace> from,
                             java.util.Map<com.digitalsubstrate.viper.NameSpace, TemplateNameSpace> built) {
        for (var nameSpace : from) {
            final var templateNameSpace = built.get(nameSpace);
            if (templateNameSpace != null)
                into.add(templateNameSpace);
        }
    }

    /**
     * What each pool's signatures reach, in the order the namespaces are emitted.
     *
     * <p>Filled after the namespaces, and from the model's own list rather than from the
     * collected set, so a pool's includes come out in the same order as a namespace's and
     * two runs of the generator produce the same file.
     */
    private void fillPoolDependencies(TemplateDefinitions definitions,
                                      DSMNameSpaceDependency nameSpaceDependency) {
        for (var pool : definitions.functionPools)
            fillInEmissionOrder(pool.dependencies,
                                nameSpaceDependency.dependencies(pool.dsmFunctionPool),
                                definitions);

        for (var pool : definitions.attachmentFunctionPools)
            fillInEmissionOrder(pool.dependencies,
                                nameSpaceDependency.dependencies(pool.dsmAttachmentFunctionPool),
                                definitions);
    }

    private static void fillInEmissionOrder(TemplateDependencies dependencies,
                                            java.util.Set<NameSpace> reached,
                                            TemplateDefinitions definitions) {
        for (var templateNameSpace : definitions.nameSpaces)
            if (reached.contains(templateNameSpace.nameSpace)) {
                dependencies.functions.add(templateNameSpace);
                dependencies.all.add(templateNameSpace);
            }
    }

    private DSMNameSpaceDependency fillNameSpaces(TemplateDefinitions definitions) {
        final var nameSpaceDependency = new DSMNameSpaceDependency();
        nameSpaceDependency.collect(inspector);
        final var nameSpaces = nameSpaceDependency.sorted();
        //nameSpaceDependency.debug();

        final var byNameSpace = new HashMap<NameSpace, TemplateNameSpace>();

        for (var nameSpace : nameSpaces) {
            final var templateNameSpace = new TemplateNameSpace(nameSpace, definitions, layout);
            byNameSpace.put(nameSpace, templateNameSpace);

            // Topological order guarantees each dependency is already built.
            fill(templateNameSpace.dependencies.types, nameSpaceDependency.typeDependencies(nameSpace), byNameSpace);
            fill(templateNameSpace.dependencies.attachments, nameSpaceDependency.attachmentDependencies(nameSpace), byNameSpace);
            fill(templateNameSpace.dependencies.all, nameSpaceDependency.dependencies(nameSpace), byNameSpace);

            for (var e : definitions.concepts)
                if (e.getDsmConcept().typeName.nameSpace.equals(nameSpace))
                    templateNameSpace.concepts.add(e);

            for (var e : definitions.clubs)
                if (e.getDsmClub().typeName.nameSpace.equals(nameSpace))
                    templateNameSpace.clubs.add(e);

            for (var e : definitions.enumerations)
                if (e.getDsmEnumeration().typeName.nameSpace.equals(nameSpace))
                    templateNameSpace.enumerations.add(e);

            for (var e : definitions.sortedStructures)
                if (e.getDsmStructure().typeName.nameSpace.equals(nameSpace))
                    templateNameSpace.sortedStructures.add(e);

            for (var e : definitions.structures)
                if (e.getDsmStructure().typeName.nameSpace.equals(nameSpace))
                    templateNameSpace.structures.add(e);

            for (var e : definitions.attachments)
                if (e.getDsmAttachment().typeName.nameSpace.equals(nameSpace))
                    templateNameSpace.attachments.add(e);

            definitions.nameSpaces.add(templateNameSpace);
        }

        return nameSpaceDependency;
    }

    // Tools Initializations
    private void populateMaps() {
        for (var structure : definitions.structures)
            structuresByTypeName.put(structure.typeName, structure);
    }

    private void registerPrimitives() {
        registerPrimitive(DSMLexicon.Void, "void", "ValueVoid");
        registerPrimitive(DSMLexicon.Bool, "bool", "ValueBool");

        registerPrimitive(DSMLexicon.UInt8, "std::uint8_t", "ValueUInt8");
        registerPrimitive(DSMLexicon.UInt16, "std::uint16_t", "ValueUInt16");
        registerPrimitive(DSMLexicon.UInt32, "std::uint32_t", "ValueUInt32");
        registerPrimitive(DSMLexicon.UInt64, "std::uint64_t", "ValueUInt64");

        registerPrimitive(DSMLexicon.Int8, "std::int8_t", "ValueInt8");
        registerPrimitive(DSMLexicon.Int16, "std::int16_t", "ValueInt16");
        registerPrimitive(DSMLexicon.Int32, "std::int32_t", "ValueInt32");
        registerPrimitive(DSMLexicon.Int64, "std::int64_t", "ValueInt64");

        registerPrimitive(DSMLexicon.Float, "float", "ValueFloat");
        registerPrimitive(DSMLexicon.Double, "double", "ValueDouble");

        registerPrimitive(DSMLexicon.BlobId, "Viper::BlobId", "ValueBlobId");
        registerPrimitive(DSMLexicon.CommitId, "Viper::CommitId", "ValueCommitId");
        registerPrimitive(DSMLexicon.UUId, "Viper::UUId", "ValueUUId");

        registerPrimitive(DSMLexicon.String, "std::string", "ValueString");
        registerPrimitive(DSMLexicon.Blob, "Viper::Blob", "ValueBlob");
    }

    private void registerPrimitive(String name, String cppType, String viperValue) {
        cppPrimitiveTypes.put(name, cppType);
        viperPrimitiveValues.put(name, viperValue);
    }

}




