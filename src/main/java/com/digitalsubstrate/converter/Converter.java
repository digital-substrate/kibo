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
    private final HashMap<TypeName, TemplateConcept> templateConceptByTypeName = new HashMap<>();
    private final HashMap<TypeName, TemplateStructure> templateStructureByTypeName = new HashMap<>();

    private final HashMap<TypeName, ArrayList<TemplateAttachment>> attachmentByConcept = new HashMap<>();

    private final HashMap<String, String> cppPrimitiveTypes = new HashMap<>();
    private final HashMap<String, String> viperPrimitiveValues = new HashMap<>();

    private final HashMap<String, DSMTypeVec> vecFunctions = new HashMap<>();
    private final HashMap<String, DSMTypeMat> matFunctions = new HashMap<>();

    private final HashMap<String, DSMTypeTuple> tupleFunctions = new HashMap<>();

    private final HashMap<String, DSMTypeOptional> optionalFunctions = new HashMap<>();
    private final HashMap<String, DSMTypeVector> vectorFunctions = new HashMap<>();
    private final HashMap<String, DSMTypeSet> setFunctions = new HashMap<>();
    private final HashMap<String, DSMTypeMap> mapFunctions = new HashMap<>();
    private final HashMap<String, DSMTypeXArray> xarrayFunctions = new HashMap<>();

    private final HashMap<String, DSMTypeVariant> variantFunctions = new HashMap<>();

    private final LiteralConverter literalConverter;
    private final TypeConverter typeConverter;

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

        populateMaps();
        registerPrimitives();
    }

    // Definitions
    public TemplateDefinitions convert() throws Exception {
        final var result = new TemplateDefinitions(generated, namespace);

        result.sortedStructures.addAll(convertStructures());
        result.enumerations.addAll(convertEnumerations());

        final var attachments = convertAttachments(definitions.attachments);
        final var concepts = convertConcepts();

        // Fill template concept isa and members
        for (var concept : concepts) {
            if (concept.getDsmConcept().parent != null) {
                final var parent = templateConceptByTypeName.get(concept.getDsmConcept().parent.typeName);
                concept.setParent(parent);
            }
            final var children = collectConceptChildren(concept.getDsmConcept());
            concept.setChildren(children);
        }

        final var functionPools = convertFunctionPool();
        final var attachmentFunctionPools = convertAttachmentFunctionPool();

        result.concepts.addAll(concepts);
        result.clubs.addAll(convertClubs());

        result.attachments.addAll(attachments);

        result.functionPools.addAll(functionPools);
        result.attachmentFunctionPools.addAll(attachmentFunctionPools);

        registerFunctionForContainer(new DSMTypeOptional(DSMTypeReference.AnyConcept));
        registerFunctionForStructures();
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

    // Enumeration
    private ArrayList<TemplateEnumeration> convertEnumerations() {
        final var result = new ArrayList<TemplateEnumeration>();
        for (var enumeration : definitions.enumerations)
            result.add(convertEnumeration(enumeration));

        return result;
    }

    private TemplateEnumeration convertEnumeration(DSMEnumeration enumeration) {
        final var type = enumeration.typeName.representation();
        final var typeSuffix = typeConverter.typeSuffix(enumeration.typeName);

        return new TemplateEnumeration(enumeration, type, typeSuffix);
    }

    // Structure
    private ArrayList<TemplateStructure> convertStructures() throws Exception {
        final var structures = structureDependency.sorted();

        final var result = new ArrayList<TemplateStructure>();
        for (var structure : structures) {
            final var templateStructure = convertStructure(structure);
            templateStructureByTypeName.put(structure.typeName, templateStructure);
            result.add(templateStructure);
        }

        return result;
    }

    private TemplateStructure convertStructure(DSMStructure structure) throws Exception {
        final var type = structure.typeName.representation();
        final var typeSuffix = typeConverter.typeSuffix(structure.typeName);
        final var isMovable = typeConverter.isStructureMovable(structure);
        TemplateStructure result = new TemplateStructure(structure, type, typeSuffix, isMovable);
        result.getFields().addAll(convertStructureFields(structure.typeName.nameSpace, structure.fields));

        return result;
    }

    private ArrayList<TemplateStructureField> convertStructureFields(NameSpace nameSpace, ArrayList<DSMStructureField> fields) throws Exception {
        final var result = new ArrayList<TemplateStructureField>();
        for (var field : fields)
            result.add(convertField(nameSpace, field));

        return result;
    }

    // Structure Field
    private TemplateStructureField convertField(NameSpace nameSpace, DSMStructureField field) throws Exception {
        final var type = typeConverter.convertType(field.type);
        final var typeInNamespace = typeConverter.convertTypeInNamespace(nameSpace, field.type);
        final var passBy = typeConverter.passByQualifier(field.type);
        final var isMovable = typeConverter.isTypeMovable(field.type);
        final var defaultValue = literalConverter.convertRootDefaultValue(field.defaultValue, field.type);
        final var typeSuffix = typeConverter.typeSuffix(field.type);
        final var isAny = typeConverter.isTypeAny(field.type);
        final var viperValue = typeConverter.viperValue(field.type);
        final var pythonType = typeConverter.templatePythonType(field.type);
        final var templateField = typeConverter.createTemplateField(field.type);

        return new TemplateStructureField(
                field, type, typeInNamespace, passBy, isMovable, defaultValue, typeSuffix,
                isAny,
                viperValue, pythonType, templateField);
    }

    // Concept members
    private ArrayList<TemplateConcept> collectConceptChildren(DSMConcept concept) {
        final var result = new ArrayList<TemplateConcept>();
        for (var c : definitions.concepts) {
            if (c.parent != null && c.parent.typeName.equals(concept.typeName)) {
                final var templateConcept = templateConceptByTypeName.get(c.typeName);
                assert templateConcept != null;
                result.add(templateConcept);
            }
        }
        result.sort(Comparator.comparing(TemplateConcept::getType));

        return result;
    }

    private ArrayList<TemplateConcept> convertConcepts() throws Exception {
        final var result = new ArrayList<TemplateConcept>();
        for (var concept : definitions.concepts) {
            registerFunctionForContainer(new DSMTypeOptional(concept.typeReference));
            final var templateConcept = convertConcept(concept);
            templateConceptByTypeName.put(concept.typeName, templateConcept);
            result.add(templateConcept);
        }

        return result;
    }

    private TemplateConcept convertConcept(DSMConcept concept) {
        final var type = typeConverter.typeForKey(concept.typeName);
        final var typeSuffix = typeConverter.typeSuffixForKey(concept.typeName);
        final var attachments = attachmentByConcept.get(concept.typeName);
        if (attachments != null)
            attachments.sort(Comparator.comparing(TemplateAttachment::getIdentifier));

        return new TemplateConcept(concept, type, typeSuffix, attachments);
    }

    // Club
    private ArrayList<TemplateClub> convertClubs() throws Exception {
        final var result = new ArrayList<TemplateClub>();
        for (var club : definitions.clubs)
            result.add(convertClub(club));

        return result;
    }

    private TemplateClub convertClub(DSMClub club) throws Exception {
        final var type = typeConverter.typeForKey(club.typeName);
        final var typeSuffix = typeConverter.typeSuffixForKey(club.typeName);

        ArrayList<TemplateConcept> members = new ArrayList<>();
        for (var member : club.members) {
            registerFunctionForContainer(new DSMTypeOptional(club.typeReference));
            final var templateConcept = templateConceptByTypeName.get(member.typeName);
            assert templateConcept != null;
            members.add(templateConcept);
        }
        members.sort(Comparator.comparing(TemplateConcept::getName));

        return new TemplateClub(club, type, typeSuffix, members);
    }

    // Attachment
    private ArrayList<TemplateAttachment> convertAttachments(ArrayList<DSMAttachment> attachments) throws Exception {
        final var result = new ArrayList<TemplateAttachment>();
        for (var attachment : attachments)
            result.add(convertAttachment(attachment));

        return result;
    }

    private TemplateAttachment convertAttachment(DSMAttachment attachment) throws Exception {
        final var isAmbiguous = isAttachmentAmbiguousInNamespace(attachment);
        final var keyType = convertAttachmentKeyType(attachment.typeName.nameSpace, attachment.keyType);
        final var documentType = convertAttachmentDocumentType(attachment.typeName.nameSpace, attachment.documentType);
        final var result = new TemplateAttachment(isAmbiguous, attachment, keyType, documentType);

        if (attachment.keyType.domain == DSMTypeReferenceDomain.CONCEPT) {
            attachmentByConcept.computeIfAbsent(attachment.keyType.typeName, k -> new ArrayList<>());
            attachmentByConcept.get(attachment.keyType.typeName).add(result);
        }

        return result;
    }

    private TemplateAttachedKeyType convertAttachmentKeyType(NameSpace nameSpace, DSMTypeReference dsmKeyType) throws Exception {
        registerFunctionForContainer(new DSMTypeSet(dsmKeyType));
        final var type = typeConverter.convertType(dsmKeyType);
        final var typeInNamespace = typeConverter.convertTypeInNamespace(nameSpace, dsmKeyType);
        final var typeSuffix = typeConverter.typeSuffix(dsmKeyType);
        final var viperValue = typeConverter.viperValue(dsmKeyType);
        final var pythonType = typeConverter.templatePythonType(dsmKeyType);

        return new TemplateAttachedKeyType(dsmKeyType.typeName, type, typeInNamespace, typeSuffix, viperValue, pythonType);
    }

    private TemplateAttachedDocumentType convertAttachmentDocumentType(NameSpace nameSpace, DSMType dsmDocumentType) throws Exception {
        registerFunctionForContainer(new DSMTypeOptional(dsmDocumentType));

        final var type = typeConverter.convertType(dsmDocumentType);
        final var typeInNameSpace = typeConverter.convertTypeInNamespace(nameSpace, dsmDocumentType);
        final var typeSuffix = typeConverter.typeSuffix(dsmDocumentType);
        final var viperValue = typeConverter.viperValue(dsmDocumentType);
        final var pythonType = typeConverter.templatePythonType(dsmDocumentType);
        final var templateStructure = findTemplateStructure(dsmDocumentType);
        final var templateField = typeConverter.createTemplateField(dsmDocumentType);
        final var useBlobId = typeConverter.useBlobId(dsmDocumentType);

        return new TemplateAttachedDocumentType(type, typeInNameSpace, typeSuffix, viperValue, pythonType, templateStructure, templateField, useBlobId);
    }

    private boolean isAttachmentAmbiguousInNamespace(DSMAttachment attachment) {
        var count = 0;
        for (var a : definitions.attachments) {
            final var sameNamespace = a.typeName.nameSpace.equals(attachment.typeName.nameSpace);
            if (!sameNamespace) continue;

            final var sameName = a.typeName.name.equals(attachment.typeName.name);
            final var sameKeyName = a.keyType.typeName.name.equals(attachment.keyType.typeName.name);
            if (sameName && sameKeyName)
                count += 1;
        }

        return count > 1;
    }


    private TemplateStructure findTemplateStructure(DSMType type) {
        if (type instanceof DSMTypeReference typeReference)
            if (typeReference.domain == DSMTypeReferenceDomain.STRUCTURE)
                return templateStructureByTypeName.get(typeReference.typeName);

        return null;
    }

    // Type Functions
    private void convertFunction(TemplateDefinitions definitions) throws Exception {

        for (var vec : vecFunctions.values()) {
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

        for (var mat : matFunctions.values()) {
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

        for (var tuple : tupleFunctions.values()) {
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

        for (var optional : optionalFunctions.values()) {
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

        for (var vector : vectorFunctions.values()) {
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

        for (var set : setFunctions.values()) {
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

        for (var map : mapFunctions.values()) {
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

        for (var xarray : xarrayFunctions.values()) {
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

        for (var variant : variantFunctions.values()) {
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

    private void registerFunctionForStructures() throws Exception {
        for (var structure : definitions.structures)
            registerFunctionForStructure(structure);
    }

    private void registerFunctionForStructure(DSMStructure structure) throws Exception {
        for (var field : structure.fields)
            registerFunctionForContainer(field.type);
    }

    public void registerFunctionForContainer(DSMType type) throws Exception {

        if (type instanceof DSMTypeVec typeVec) {
            final var key = typeConverter.typeSuffix(typeVec);
            vecFunctions.put(key, typeVec);
            registerFunctionForContainer(typeVec.elementType);

        } else if (type instanceof DSMTypeMat typeMat) {
            final var key = typeConverter.typeSuffix(typeMat);
            matFunctions.put(key, typeMat);
            registerFunctionForContainer(typeMat.elementType);

        } else if (type instanceof DSMTypeTuple typeTuple) {
            final var key = typeConverter.typeSuffix(typeTuple);
            tupleFunctions.put(key, typeTuple);
            for (var memberType : typeTuple.types)
                registerFunctionForContainer(memberType);

        } else if (type instanceof DSMTypeOptional typeOptional) {
            final var key = typeConverter.typeSuffix(typeOptional);
            optionalFunctions.put(key, typeOptional);
            registerFunctionForContainer(typeOptional.elementType);

        } else if (type instanceof DSMTypeVector typeVector) {
            final var key = typeConverter.typeSuffix(typeVector);
            vectorFunctions.put(key, typeVector);
            registerFunctionForContainer(typeVector.elementType);

        } else if (type instanceof DSMTypeSet typeSet) {
            final var key = typeConverter.typeSuffix(typeSet);
            setFunctions.put(key, typeSet);
            registerFunctionForContainer(typeSet.elementType);

        } else if (type instanceof DSMTypeMap typeMap) {
            final var key = typeConverter.typeSuffix(typeMap);
            mapFunctions.put(key, typeMap);
            registerFunctionForContainer(typeMap.keyType);
            registerFunctionForContainer(typeMap.elementType);
            registerFunctionForContainer(new DSMTypeSet(typeMap.keyType));

        } else if (type instanceof DSMTypeXArray typeXArray) {
            final var key = typeConverter.typeSuffix(typeXArray);
            xarrayFunctions.put(key, typeXArray);
            registerFunctionForContainer(typeXArray.elementType);
            registerFunctionForContainer(new DSMTypeVector(typeXArray.elementType));

        } else if (type instanceof DSMTypeVariant typeVariant) {
            final var key = typeConverter.typeSuffix(typeVariant);
            variantFunctions.put(key, typeVariant);
            for (var memberType : typeVariant.types)
                registerFunctionForContainer(memberType);
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
        registerFunctionForContainer(function.prototype.returnType);

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
        registerFunctionForContainer(function.prototype.returnType);

        return new TemplateAttachmentFunction(
                function, type, typeSuffix, parameters,
                returnViperValue, templatePythonType);
    }

    // Parameters
    ArrayList<TemplateFunctionParameter> convertFunctionParameters(ArrayList<DSMFunctionPrototypeParameter> parameters) throws Exception {
        final var result = new ArrayList<TemplateFunctionParameter>();
        for (var parameter : parameters) {
            registerFunctionForContainer(parameter.type);
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




