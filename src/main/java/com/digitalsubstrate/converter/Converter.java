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

        final var functionPools = convertFunctionPool();
        final var attachmentFunctionPools = convertAttachmentFunctionPool();

        result.concepts.addAll(concepts);
        result.clubs.addAll(entityConverter.convertClubs());

        result.attachments.addAll(attachments);

        result.functionPools.addAll(functionPools);
        result.attachmentFunctionPools.addAll(attachmentFunctionPools);

        functionRegistrar.registerFunctionForContainer(new DSMTypeOptional(DSMTypeReference.AnyConcept));
        functionRegistrar.registerFunctionForStructures();
        convertFunction(result);

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

    // Type Functions
    private void convertFunction(TemplateDefinitions definitions) throws Exception {

        for (var vec : functionRegistrar.vecFunctions.values()) {
            final var type = typeConverter.convertType(vec);
            final var typeSuffix = typeConverter.typeSuffix(vec);
            final var elementTypeSuffix = typeConverter.typeSuffix(vec.elementType);
            final var dsmType = vec.representation();
            final var pythonType = typeConverter.templatePythonType(vec);
            final var pythonElementType = typeConverter.templatePythonType(vec.elementType);

            definitions.vecFunctions.add(new TemplateVecFunction(
                    type, vec.size, typeSuffix, elementTypeSuffix,
                    dsmType,
                    pythonType, pythonElementType));
        }

        for (var mat : functionRegistrar.matFunctions.values()) {
            final var type = typeConverter.convertType(mat);
            final var typeSuffix = typeConverter.typeSuffix(mat);
            final var elementTypeSuffix = typeConverter.typeSuffix(mat.elementType);
            final var dsmType = mat.representation();
            final var pythonType = typeConverter.templatePythonType(mat);
            final var pythonElementType = typeConverter.templatePythonType(mat.elementType);

            definitions.matFunctions.add(new TemplateMatFunction(
                    type, mat.columns, mat.rows, typeSuffix, elementTypeSuffix,
                    dsmType,
                    pythonType, pythonElementType));
        }

        for (var tuple : functionRegistrar.tupleFunctions.values()) {
            final var type = typeConverter.convertType(tuple);
            final var typeSuffix = typeConverter.typeSuffix(tuple);
            final var members = new ArrayList<TemplateType>();
            final var dsmType = tuple.representation();
            final var pythonType = typeConverter.templatePythonType(tuple);
            final var pythonMembers = new ArrayList<TemplatePythonType>();

            for (var memberType : tuple.types) {
                final var mType = typeConverter.convertType(memberType);
                final var mTypeSuffix = typeConverter.typeSuffix(memberType);
                members.add(new TemplateType(mType, mTypeSuffix));
                pythonMembers.add(typeConverter.templatePythonType(memberType));
            }

            definitions.tupleFunctions.add(new TemplateTupleFunction(
                    type, typeSuffix, members,
                    dsmType,
                    pythonType, pythonMembers));
        }

        for (var optional : functionRegistrar.optionalFunctions.values()) {
            final var type = typeConverter.convertType(optional);
            final var typeSuffix = typeConverter.typeSuffix(optional);
            final var elementType = typeConverter.convertType(optional.elementType);
            final var elementTypeSuffix = typeConverter.typeSuffix(optional.elementType);
            final var dsmType = optional.representation();
            final var pythonType = typeConverter.templatePythonType(optional);
            final var pythonElementType = typeConverter.templatePythonType(optional.elementType);

            definitions.optionalFunctions.add(new TemplateOptionalFunction(
                    type, typeSuffix, elementType, elementTypeSuffix,
                    dsmType,
                    pythonType, pythonElementType));
        }

        for (var vector : functionRegistrar.vectorFunctions.values()) {
            final var type = typeConverter.convertType(vector);
            final var typeSuffix = typeConverter.typeSuffix(vector);
            final var elementTypeSuffix = typeConverter.typeSuffix(vector.elementType);
            final var dsmType = vector.representation();
            final var pythonType = typeConverter.templatePythonType(vector);
            final var pythonElementType = typeConverter.templatePythonType(vector.elementType);

            definitions.vectorFunctions.add(new TemplateVectorFunction(
                    type, typeSuffix, elementTypeSuffix,
                    dsmType,
                    pythonType, pythonElementType));
        }

        for (var set : functionRegistrar.setFunctions.values()) {
            final var type = typeConverter.convertType(set);
            final var typeSuffix = typeConverter.typeSuffix(set);
            final var elementTypeSuffix = typeConverter.typeSuffix(set.elementType);
            final var dsmType = set.representation();
            final var pythonType = typeConverter.templatePythonType(set);
            final var pythonElementType = typeConverter.templatePythonType(set.elementType);

            definitions.setFunctions.add(new TemplateSetFunction(
                    type, typeSuffix, elementTypeSuffix,
                    dsmType,
                    pythonType, pythonElementType));
        }

        for (var map : functionRegistrar.mapFunctions.values()) {
            final var type = typeConverter.convertType(map);
            final var typeSuffix = typeConverter.typeSuffix(map);
            final var keyTypeSuffix = typeConverter.typeSuffix(map.keyType);
            final var elementTypeSuffix = typeConverter.typeSuffix(map.elementType);
            final var dsmType = map.representation();
            final var pythonType = typeConverter.templatePythonType(map);
            final var pythonKeyType = typeConverter.templatePythonType(map.keyType);
            final var pythonElementType = typeConverter.templatePythonType(map.elementType);

            definitions.mapFunctions.add(new TemplateMapFunction(
                    type, typeSuffix, keyTypeSuffix, elementTypeSuffix,
                    dsmType,
                    pythonType, pythonKeyType, pythonElementType));
        }

        for (var xarray : functionRegistrar.xarrayFunctions.values()) {
            final var type = typeConverter.convertType(xarray);
            final var typeSuffix = typeConverter.typeSuffix(xarray);
            final var elementType = typeConverter.convertType(xarray.elementType);
            final var elementTypeSuffix = typeConverter.typeSuffix(xarray.elementType);
            final var dsmType = xarray.representation();
            final var pythonType = typeConverter.templatePythonType(xarray);
            final var pythonElementType = typeConverter.templatePythonType(xarray.elementType);

            definitions.xarrayFunctions.add(new TemplateXArrayFunction(type, typeSuffix, elementType, elementTypeSuffix,
                    dsmType,
                    pythonType, pythonElementType));
        }

        for (var variant : functionRegistrar.variantFunctions.values()) {
            final var type = typeConverter.convertType(variant);
            final var typeSuffix = typeConverter.typeSuffix(variant);
            final var members = new ArrayList<TemplateType>();
            final var dsmType = variant.representation();
            final var pythonType = typeConverter.templatePythonType(variant);
            final var pythonMembers = new ArrayList<TemplatePythonType>();

            for (var memberType : variant.types) {
                final var mType = typeConverter.convertType(memberType);
                final var mTypeSuffix = typeConverter.typeSuffix(memberType);
                members.add(new TemplateType(mType, mTypeSuffix));
                pythonMembers.add(typeConverter.templatePythonType(memberType));
            }

            definitions.variantFunctions.add(new TemplateVariantFunction(
                    type, typeSuffix, members,
                    dsmType,
                    pythonType, pythonMembers));
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

    // Predicates
    // Function Pool
    private ArrayList<TemplateFunctionPool> convertFunctionPool() throws Exception {
        final var result = new ArrayList<TemplateFunctionPool>();
        for (var pool : definitions.functionPools)
            result.add(convertFunctionPool(pool));

        return result;
    }

    private TemplateFunctionPool convertFunctionPool(DSMFunctionPool pool) throws Exception {
        final var functions = new ArrayList<TemplateFunction>();
        for (var function : pool.functions)
            functions.add(convertFunction(function));

        return new TemplateFunctionPool(functions, pool);
    }

    private TemplateFunction convertFunction(DSMFunction function) throws Exception {
        final var parameters = convertFunctionParameters(function.prototype.parameters);
        final var type = typeConverter.convertType(function.prototype.returnType);
        final var typeSuffix = typeConverter.typeSuffix(function.prototype.returnType);
        final var returnViperValue = typeConverter.viperValue(function.prototype.returnType);
        final var returnPythonType = typeConverter.templatePythonType(function.prototype.returnType);
        functionRegistrar.registerFunctionForContainer(function.prototype.returnType);

        return new TemplateFunction(
                function, type, typeSuffix, parameters,
                returnViperValue, returnPythonType);
    }

    // AttachmentFunction Pool
    private ArrayList<TemplateAttachmentFunctionPool> convertAttachmentFunctionPool() throws Exception {
        final var result = new ArrayList<TemplateAttachmentFunctionPool>();
        for (var pool : definitions.attachmentFunctionPools)
            result.add(convertAttachmentFunctionPool(pool));

        return result;
    }

    private TemplateAttachmentFunctionPool convertAttachmentFunctionPool(DSMAttachmentFunctionPool pool) throws Exception {
        final var functions = new ArrayList<TemplateAttachmentFunction>();
        for (var function : pool.functions)
            functions.add(convertAttachmentFunction(function));

        return new TemplateAttachmentFunctionPool(functions, pool);
    }

    private TemplateAttachmentFunction convertAttachmentFunction(DSMAttachmentFunction function) throws Exception {
        final var parameters = convertFunctionParameters(function.prototype.parameters);
        final var type = typeConverter.convertType(function.prototype.returnType);
        final var typeSuffix = typeConverter.typeSuffix(function.prototype.returnType);
        final var returnViperValue = typeConverter.viperValue(function.prototype.returnType);
        final var templatePythonType = typeConverter.templatePythonType(function.prototype.returnType);
        functionRegistrar.registerFunctionForContainer(function.prototype.returnType);

        return new TemplateAttachmentFunction(
                function, type, typeSuffix, parameters,
                returnViperValue, templatePythonType);
    }

    // Parameters
    ArrayList<TemplateFunctionParameter> convertFunctionParameters(ArrayList<DSMFunctionPrototypeParameter> parameters) throws Exception {
        final var result = new ArrayList<TemplateFunctionParameter>();
        for (var parameter : parameters) {
            functionRegistrar.registerFunctionForContainer(parameter.type);
            result.add(convertFunctionParameter(parameter));
        }

        return result;
    }

    TemplateFunctionParameter convertFunctionParameter(DSMFunctionPrototypeParameter parameter) throws Exception {
        final var type = typeConverter.convertType(parameter.type);
        final var passBy = typeConverter.passByQualifier(parameter.type);
        final var typeSuffix = typeConverter.typeSuffix(parameter.type);
        final var viperValue = typeConverter.viperValue(parameter.type);
        final var templatePythonType = typeConverter.templatePythonType(parameter.type);

        return new TemplateFunctionParameter(parameter, type, passBy, typeSuffix, viperValue, templatePythonType);
    }

}




