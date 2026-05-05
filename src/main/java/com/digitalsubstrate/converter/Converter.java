package com.digitalsubstrate.converter;

import com.digitalsubstrate.template.*;
import com.digitalsubstrate.viper.NameSpace;
import com.digitalsubstrate.viper.TypeName;
import com.digitalsubstrate.viper.dsm.*;

import java.util.ArrayList;
import java.util.Comparator;
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
    private final FunctionConverter functionConverter;

    public final String generated;
    public final DSMDefinitions definitions;
    public final DSMDefinitionsInspector inspector;
    public final String namespace;
    public boolean hasTypeAny;

    public Converter(String generated, DSMDefinitions definitions, String namespace) {
        this.generated = generated;
        this.definitions = definitions;
        this.namespace = namespace;
        this.hasTypeAny = false;
        this.inspector = new DSMDefinitionsInspector(definitions);
        this.structureDependency = new DSMStructureDependency(this.inspector);
        this.literalConverter = new LiteralConverter(structuresByTypeName);
        this.typeConverter = new TypeConverter(cppPrimitiveTypes, viperPrimitiveValues, structuresByTypeName);
        this.functionRegistrar = new FunctionRegistrar(definitions, typeConverter);
        this.entityConverter = new EntityConverter(definitions, structureDependency, typeConverter, literalConverter, functionRegistrar);
        this.functionConverter = new FunctionConverter(definitions, typeConverter, functionRegistrar);

        populateMaps();
        registerPrimitives();
    }

    // Definitions
    public TemplateDefinitions convert() throws Exception {
        final var result = new TemplateDefinitions(generated, namespace);

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

        final var functionPools = functionConverter.convertFunctionPool();
        final var attachmentFunctionPools = functionConverter.convertAttachmentFunctionPool();

        result.concepts.addAll(concepts);
        result.clubs.addAll(entityConverter.convertClubs());

        result.attachments.addAll(attachments);

        result.functionPools.addAll(functionPools);
        result.attachmentFunctionPools.addAll(attachmentFunctionPools);

        functionRegistrar.registerFunctionForContainer(new DSMTypeOptional(DSMTypeReference.AnyConcept));
        functionRegistrar.registerFunctionForStructures();
        functionConverter.emitContainerFunctions(result);

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

        // Create NameSpaces
        fillNameSpaces(result);
        return result;
    }

    private void fillNameSpaces(TemplateDefinitions definitions) {
        final var nameSpaceDependency = new DSMNameSpaceDependency();
        nameSpaceDependency.collect(inspector);
        final var nameSpaces = nameSpaceDependency.sorted();
        //nameSpaceDependency.debug();

        for (var nameSpace : nameSpaces) {
            final var templateNameSpace = new TemplateNameSpace(nameSpace);

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




