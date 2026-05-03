// Copyright (c) Digital Substrate 2026, All rights reserved.

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
        final var typeSuffix = typeSuffix(enumeration.typeName);

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
        final var typeSuffix = typeSuffix(structure.typeName);
        final var isMovable = isStructureMovable(structure);
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
        final var type = convertType(field.type);
        final var typeInNamespace = convertTypeInNamespace(nameSpace, field.type);
        final var passBy = passByQualifier(field.type);
        final var isMovable = isTypeMovable(field.type);
        final var defaultValue = convertRootDefaultValue(field.defaultValue, field.type);
        final var typeSuffix = typeSuffix(field.type);
        final var isAny = isTypeAny(field.type);
        final var viperValue = viperValue(field.type);
        final var pythonType = templatePythonType(field.type);
        final var templateField = createTemplateField(field.type);

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
        final var type = typeForKey(concept.typeName);
        final var typeSuffix = typeSuffixForKey(concept.typeName);
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
        final var type = typeForKey(club.typeName);
        final var typeSuffix = typeSuffixForKey(club.typeName);

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
        final var type = convertType(dsmKeyType);
        final var typeInNamespace = convertTypeInNamespace(nameSpace, dsmKeyType);
        final var typeSuffix = typeSuffix(dsmKeyType);
        final var viperValue = viperValue(dsmKeyType);
        final var pythonType = templatePythonType(dsmKeyType);

        return new TemplateAttachedKeyType(dsmKeyType.typeName, type, typeInNamespace, typeSuffix, viperValue, pythonType);
    }

    private TemplateAttachedDocumentType convertAttachmentDocumentType(NameSpace nameSpace, DSMType dsmDocumentType) throws Exception {
        registerFunctionForContainer(new DSMTypeOptional(dsmDocumentType));

        final var type = convertType(dsmDocumentType);
        final var typeInNameSpace = convertTypeInNamespace(nameSpace, dsmDocumentType);
        final var typeSuffix = typeSuffix(dsmDocumentType);
        final var viperValue = viperValue(dsmDocumentType);
        final var pythonType = templatePythonType(dsmDocumentType);
        final var templateStructure = findTemplateStructure(dsmDocumentType);
        final var templateField = createTemplateField(dsmDocumentType);
        final var useBlobId = useBlobId(dsmDocumentType);

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

    private String convertRootDefaultValue(DSMLiteral literal, DSMType type) {
        if (literal instanceof DSMLiteralValue literalValue) {
            if (literalValue.domain == DSMLiteralDomain.NONE)
                return "{}";

            return "{" + convertDefaultValue(literal, type) + "}";
        }

        return convertDefaultValue(literal, type);
    }

    private String convertDefaultValue(DSMLiteral literal, DSMType type) {
        if (type instanceof DSMTypeVec typeVec)
            return convertDefaultValueVec(literal, typeVec);

        if (type instanceof DSMTypeMat typeMat)
            return convertDefaultValueMat(literal, typeMat);

        if (type instanceof DSMTypeTuple typeTuple)
            return convertDefaultValueTuple(literal, typeTuple);

        if (type instanceof DSMTypeOptional typeOptional)
            return convertDefaultValueOptional(literal, typeOptional);

        if (type instanceof DSMTypeVector typeVector)
            return convertDefaultValueVector(literal, typeVector);

        if (type instanceof DSMTypeSet typeSet)
            return convertDefaultValueSet(literal, typeSet);

        if (type instanceof DSMTypeMap typeMap)
            return convertDefaultValueMap(literal, typeMap);

        if (type instanceof DSMTypeXArray typeXArray)
            return convertDefaultValueXArray(literal, typeXArray);

        if (type instanceof DSMTypeReference typeReference)
            return convertDefaultValueTypeReference(literal, typeReference);

        return "";
    }

    // Literal
    String convertLiteralList(DSMLiteral literal, DSMType elementType) {
        final var literalList = (DSMLiteralList) literal;
        final var elements = new ArrayList<String>();
        for (var member : literalList.members)
            elements.add(convertDefaultValue(member, elementType));

        return "{" + String.join(", ", elements) + "}";
    }

    String convertDefaultValueVec(DSMLiteral literal, DSMTypeVec typeVec) {
        return convertLiteralList(literal, typeVec.elementType);
    }

    String convertDefaultValueMat(DSMLiteral literal, DSMTypeMat typeMat) {
        final var literalList = (DSMLiteralList) literal;
        final var colLiterals = new ArrayList<String>();
        for (var columnMember : literalList.members)
            colLiterals.add(convertLiteralList(columnMember, typeMat.elementType));


        return "{{" + String.join(", ", colLiterals) + "}}";
    }

    String convertDefaultValueTuple(DSMLiteral literal, DSMTypeTuple typeTuple) {
        final var literalList = (DSMLiteralList) literal;
        final var elements = new ArrayList<String>();
        for (var i = 0; i < typeTuple.types.size(); i++) {
            final var type = typeTuple.types.get(i);
            final var member = literalList.members.get(i);
            elements.add(convertDefaultValue(member, type));
        }

        return "{" + String.join(", ", elements) + "}";
    }

    String convertDefaultValueOptional(DSMLiteral literal, DSMTypeOptional typeOptional) {
        return convertDefaultValue(literal, typeOptional.elementType);
    }

    String convertDefaultValueVector(DSMLiteral literal, DSMTypeVector typeVector) {
        return convertLiteralList(literal, typeVector.elementType);
    }

    String convertDefaultValueSet(DSMLiteral literal, DSMTypeSet typeSet) {
        return convertLiteralList(literal, typeSet.elementType);
    }

    String convertDefaultValueMap(DSMLiteral literal, DSMTypeMap typeMap) {
        var literalList = (DSMLiteralList) literal;
        var entryLiterals = new ArrayList<String>();
        for (var entry : literalList.members) {
            final var entryLiteralList = (DSMLiteralList) entry;
            final var key = convertDefaultValue(entryLiteralList.members.get(0), typeMap.keyType);
            final var value = convertDefaultValue(entryLiteralList.members.get(1), typeMap.elementType);
            entryLiterals.add("{" + key + ", " + value + "}");
        }

        return "{" + String.join(", ", entryLiterals) + "}";
    }

    String convertDefaultValueXArray(DSMLiteral literal, DSMTypeXArray typeXArray) {
        return convertLiteralList(literal, typeXArray.elementType);
    }

    String convertDefaultValueTypeReference(DSMLiteral literal, DSMTypeReference typeReference) {
        switch (typeReference.domain) {
            case ANY, CONCEPT, CLUB, ANY_CONCEPT -> {
                return "";
            }
            case PRIMITIVE, ENUMERATION -> {
                return convertDefaultValueTypeReferenceDomain(literal, typeReference);
            }
            case STRUCTURE -> {
                return convertDefaultValueTypeReferenceStructure(literal, typeReference);
            }
        }

        return "";
    }

    String convertDefaultValueTypeReferenceStructure(DSMLiteral literal, DSMTypeReference typeReference) {
        final var literalList = (DSMLiteralList) literal;
        final var typeStructure = structuresByTypeName.get(typeReference.typeName);

        final var elements = new ArrayList<String>();
        for (var i = 0; i < typeStructure.fields.size(); i++) {
            final var type = typeStructure.fields.get(i).type;
            final var member = literalList.members.get(i);
            elements.add(convertDefaultValue(member, type));
        }

        return "{" + String.join(", ", elements) + "}";
    }

    String convertDefaultValueTypeReferenceDomain(DSMLiteral literal, DSMTypeReference typeReference) {
        if (literal instanceof DSMLiteralValue literalValue) {
            switch (literalValue.domain) {
                case NONE -> {
                    return "";
                }
                case BOOLEAN, INTEGER, DOUBLE -> {
                    return literalValue.value;
                }
                case FLOAT -> {
                    var value = literalValue.value;
                    return String.format("%sf", value);
                }
                case STRING -> {
                    return String.format("\"%s\"", literalValue.value);
                }
                case UUID -> {
                    return String.format("Viper::UUId::parse(\"%s\")", literalValue.value);
                }
                case ENUMERATION_CASE -> {
                    return String.format("%s::%s", typeReference.typeName.name, TemplateTool.uf(literalValue.value));
                }
            }
        }

        return "";
    }

    private boolean isTypeAny(DSMType type) {
        if (type instanceof DSMTypeReference typeReference) {
            return typeReference.domain == DSMTypeReferenceDomain.ANY;
        }

        return false;
    }

    private String convertType(DSMType type) throws ConvertException {
        if (type instanceof DSMTypeKey typeKey)
            return convertType(typeKey.elementType);

        if (type instanceof DSMTypeVec typeVec)
            return String.format("std::array<%s, %d>", convertType(typeVec.elementType), typeVec.size);

        if (type instanceof DSMTypeMat typeMat)
            return String.format("std::array<std::array<%s, %d>, %d>", convertType(typeMat.elementType), typeMat.rows, typeMat.columns);

        if (type instanceof DSMTypeTuple typeTuple) {
            final var memberTypes = new ArrayList<String>();
            for (var memberType : typeTuple.types)
                memberTypes.add(convertType(memberType));

            return "std::tuple<" + String.join(", ", memberTypes) + ">";
        }

        if (type instanceof DSMTypeOptional typeOptional)
            return String.format("std::optional<%s>", convertType(typeOptional.elementType));

        if (type instanceof DSMTypeVector typeVector)
            return String.format("std::vector<%s>", convertType(typeVector.elementType));

        if (type instanceof DSMTypeSet typeSet)
            return String.format("std::set<%s>", convertType(typeSet.elementType));

        if (type instanceof DSMTypeMap typeMap)
            return String.format("std::map<%s, %s>", convertType(typeMap.keyType), convertType(typeMap.elementType));

        if (type instanceof DSMTypeVariant typeVariant) {
            final var memberTypes = new ArrayList<String>();
            for (var memberType : typeVariant.types)
                memberTypes.add(convertType(memberType));

            return "std::variant<" + String.join(", ", memberTypes) + ">";
        }

        if (type instanceof DSMTypeXArray typeXArray)
            return String.format("Viper::XArray<%s>", convertType(typeXArray.elementType));

        if (type instanceof DSMTypeReference typeReference) {
            switch (typeReference.domain) {
                case PRIMITIVE -> {
                    return convertPrimitiveType(typeReference.typeName.name);
                }
                case ENUMERATION, STRUCTURE -> {
                    return typeReference.representation();
                }
                case CONCEPT, CLUB -> {
                    return String.format("%sKey", typeReference.representation());
                }
                case ANY_CONCEPT -> {
                    return "AnyConceptKey";
                }
                case ANY -> {
                    return "Viper::Any";
                }
            }
        }

        throw new ConvertException(String.format("convertType: type '%s' is not handled.", type.getClass().getName()));
    }

    private String convertTypeInNamespace(NameSpace nameSpace, DSMType type) throws ConvertException {
        if (type instanceof DSMTypeKey typeKey)
            return convertTypeInNamespace(nameSpace, typeKey.elementType);

        if (type instanceof DSMTypeVec typeVec)
            return String.format("std::array<%s, %d>", convertTypeInNamespace(nameSpace, typeVec.elementType), typeVec.size);

        if (type instanceof DSMTypeMat typeMat)
            return String.format("std::array<std::array<%s, %d>, %d>", convertTypeInNamespace(nameSpace, typeMat.elementType), typeMat.rows, typeMat.columns);

        if (type instanceof DSMTypeTuple typeTuple) {
            var memberTypes = new ArrayList<String>();
            for (var memberType : typeTuple.types)
                memberTypes.add(convertTypeInNamespace(nameSpace, memberType));

            return "std::tuple<" + String.join(", ", memberTypes) + ">";
        }

        if (type instanceof DSMTypeOptional typeOptional)
            return String.format("std::optional<%s>", convertTypeInNamespace(nameSpace, typeOptional.elementType));

        if (type instanceof DSMTypeVector typeVector)
            return String.format("std::vector<%s>", convertTypeInNamespace(nameSpace, typeVector.elementType));

        if (type instanceof DSMTypeSet typeSet)
            return String.format("std::set<%s>", convertTypeInNamespace(nameSpace, typeSet.elementType));

        if (type instanceof DSMTypeMap typeMap)
            return String.format("std::map<%s, %s>",
                    convertTypeInNamespace(nameSpace, typeMap.keyType),
                    convertTypeInNamespace(nameSpace, typeMap.elementType));

        if (type instanceof DSMTypeVariant typeVariant) {
            var memberTypes = new ArrayList<String>();
            for (var memberType : typeVariant.types)
                memberTypes.add(convertTypeInNamespace(nameSpace, memberType));

            return "std::variant<" + String.join(", ", memberTypes) + ">";
        }

        if (type instanceof DSMTypeXArray typeXArray)
            return String.format("Viper::XArray<%s>", convertTypeInNamespace(nameSpace, typeXArray.elementType));

        if (type instanceof DSMTypeReference typeReference) {
            switch (typeReference.domain) {
                case PRIMITIVE -> {
                    return convertPrimitiveType(typeReference.typeName.name);
                }
                case ENUMERATION, STRUCTURE -> {
                    if (typeReference.typeName.nameSpace.equals(nameSpace))
                        return typeReference.typeName.name;
                    return typeReference.representation();
                }
                case CONCEPT, CLUB -> {
                    if (typeReference.typeName.nameSpace.name.equals(nameSpace.name))
                        return String.format("%sKey", typeReference.typeName.name);
                    return String.format("%sKey", typeReference.representation());
                }
                case ANY_CONCEPT -> {
                    return "AnyConceptKey";
                }
                case ANY -> {
                    return "Viper::Any";
                }
            }
        }

        throw new ConvertException(String.format("convertType: type '%s' is not handled.", type.getClass().getName()));
    }

    private String convertPrimitiveType(String identifier) throws ConvertException {
        final var type = cppPrimitiveTypes.get(identifier);
        if (type != null)
            return type;

        throw new ConvertException(String.format("convertPrimitiveType: '%s' is not handled.", identifier));
    }

    private String passByQualifier(DSMType type) {
        final var qualifier = " const &";

        if (type instanceof DSMTypeReference typeReference) {
            switch (typeReference.domain) {
                case PRIMITIVE -> {
                    if ((isBlob(typeReference.typeName)
                            || isBlobId(typeReference.typeName)
                            || isCommitId(typeReference.typeName)
                            || isUUId(typeReference.typeName)
                            || isString(typeReference.typeName))) {
                        return qualifier;
                    }
                    return "";
                }
                case ENUMERATION -> {
                    return "";
                }
                case STRUCTURE, CONCEPT, CLUB, ANY_CONCEPT, ANY -> {
                    return qualifier;
                }
            }
        }

        return qualifier;
    }

    private String typeSuffix(DSMType type) throws Exception {
        if (type instanceof DSMTypeKey typeKey) {
            return typeSuffix(typeKey.elementType);
        }

        if (type instanceof DSMTypeVec typeVec)
            return String.format("_vec%d%s", typeVec.size, typeSuffix(typeVec.elementType));

        if (type instanceof DSMTypeMat typeMat)
            return String.format("_mat%dx%d%s", typeMat.columns, typeMat.rows, typeSuffix(typeMat.elementType));

        if (type instanceof DSMTypeTuple typeTuple) {
            final var memberSuffixes = new ArrayList<String>();
            for (var memberType : typeTuple.types)
                memberSuffixes.add(typeSuffix(memberType));

            return "_tuple" + String.join("", memberSuffixes);
        }

        if (type instanceof DSMTypeOptional typeOptional)
            return String.format("_optional%s", typeSuffix(typeOptional.elementType));

        if (type instanceof DSMTypeVector typeVector)
            return String.format("_vector%s", typeSuffix(typeVector.elementType));

        if (type instanceof DSMTypeMap typeMap)
            return String.format("_map%s_to%s", typeSuffix(typeMap.keyType), typeSuffix(typeMap.elementType));

        if (type instanceof DSMTypeSet typeSet)
            return String.format("_set%s", typeSuffix(typeSet.elementType));

        if (type instanceof DSMTypeXArray typeXArray)
            return String.format("_xarray%s", typeSuffix(typeXArray.elementType));

        if (type instanceof DSMTypeVariant typeVariant) {
            var memberSuffixes = new ArrayList<String>();
            for (var memberType : typeVariant.types)
                memberSuffixes.add(typeSuffix(memberType));

            return "_variant" + String.join("", memberSuffixes);
        }

        if (type instanceof DSMTypeReference typeReference) {
            switch (typeReference.domain) {
                case PRIMITIVE, ENUMERATION, STRUCTURE -> {
                    return typeSuffix(typeReference.typeName);
                }
                case CONCEPT, CLUB -> {
                    return typeSuffixForKey(typeReference.typeName);
                }
                case ANY_CONCEPT -> {
                    return "_AnyConceptKey";
                }
                case ANY -> {
                    return "_any";
                }
            }
        }

        throw new ConvertException(String.format("typeSuffix: Type '%s' not handled.", type.representation()));
    }

    private String typeForKey(TypeName typeName) {
        return String.format("%sKey", typeName.representation());
    }

    private String typeSuffixForKey(TypeName typeName) {
        return String.format("_%s_%sKey", typeName.nameSpace.name, typeName.name);
    }

    private String typeSuffix(TypeName typeName) {
        if (typeName.nameSpace.isGlobal())
            return String.format("_%s", typeName.name);
        return String.format("_%s_%s", typeName.nameSpace.name, typeName.name);
    }

    private String viperPrimitiveValue(String name) throws Exception {
        final var result = viperPrimitiveValues.get(name);
        if (result == null)
            throw new ConvertException(String.format("viperPrimitiveValue: %s is not handled", name));

        return result;
    }

    // Type Functions
    private void convertFunction(TemplateDefinitions definitions) throws Exception {

        for (var vec : vecFunctions.values()) {
            final var type = convertType(vec);
            final var typeSuffix = typeSuffix(vec);
            final var elementTypeSuffix = typeSuffix(vec.elementType);
            final var dsmType = vec.representation();
            final var pythonType = templatePythonType(vec);
            final var pythonElementType = templatePythonType(vec.elementType);

            definitions.vecFunctions.add(new TemplateVecFunction(
                    type, vec.size, typeSuffix, elementTypeSuffix,
                    dsmType,
                    pythonType, pythonElementType));
        }

        for (var mat : matFunctions.values()) {
            final var type = convertType(mat);
            final var typeSuffix = typeSuffix(mat);
            final var elementTypeSuffix = typeSuffix(mat.elementType);
            final var dsmType = mat.representation();
            final var pythonType = templatePythonType(mat);
            final var pythonElementType = templatePythonType(mat.elementType);

            definitions.matFunctions.add(new TemplateMatFunction(
                    type, mat.columns, mat.rows, typeSuffix, elementTypeSuffix,
                    dsmType,
                    pythonType, pythonElementType));
        }

        for (var tuple : tupleFunctions.values()) {
            final var type = convertType(tuple);
            final var typeSuffix = typeSuffix(tuple);
            final var members = new ArrayList<TemplateType>();
            final var dsmType = tuple.representation();
            final var pythonType = templatePythonType(tuple);
            final var pythonMembers = new ArrayList<TemplatePythonType>();

            for (var memberType : tuple.types) {
                final var mType = convertType(memberType);
                final var mTypeSuffix = typeSuffix(memberType);
                members.add(new TemplateType(mType, mTypeSuffix));
                pythonMembers.add(templatePythonType(memberType));
            }

            definitions.tupleFunctions.add(new TemplateTupleFunction(
                    type, typeSuffix, members,
                    dsmType,
                    pythonType, pythonMembers));
        }

        for (var optional : optionalFunctions.values()) {
            final var type = convertType(optional);
            final var typeSuffix = typeSuffix(optional);
            final var elementType = convertType(optional.elementType);
            final var elementTypeSuffix = typeSuffix(optional.elementType);
            final var dsmType = optional.representation();
            final var pythonType = templatePythonType(optional);
            final var pythonElementType = templatePythonType(optional.elementType);

            definitions.optionalFunctions.add(new TemplateOptionalFunction(
                    type, typeSuffix, elementType, elementTypeSuffix,
                    dsmType,
                    pythonType, pythonElementType));
        }

        for (var vector : vectorFunctions.values()) {
            final var type = convertType(vector);
            final var typeSuffix = typeSuffix(vector);
            final var elementTypeSuffix = typeSuffix(vector.elementType);
            final var dsmType = vector.representation();
            final var pythonType = templatePythonType(vector);
            final var pythonElementType = templatePythonType(vector.elementType);

            definitions.vectorFunctions.add(new TemplateVectorFunction(
                    type, typeSuffix, elementTypeSuffix,
                    dsmType,
                    pythonType, pythonElementType));
        }

        for (var set : setFunctions.values()) {
            final var type = convertType(set);
            final var typeSuffix = typeSuffix(set);
            final var elementTypeSuffix = typeSuffix(set.elementType);
            final var dsmType = set.representation();
            final var pythonType = templatePythonType(set);
            final var pythonElementType = templatePythonType(set.elementType);

            definitions.setFunctions.add(new TemplateSetFunction(
                    type, typeSuffix, elementTypeSuffix,
                    dsmType,
                    pythonType, pythonElementType));
        }

        for (var map : mapFunctions.values()) {
            final var type = convertType(map);
            final var typeSuffix = typeSuffix(map);
            final var keyTypeSuffix = typeSuffix(map.keyType);
            final var elementTypeSuffix = typeSuffix(map.elementType);
            final var dsmType = map.representation();
            final var pythonType = templatePythonType(map);
            final var pythonKeyType = templatePythonType(map.keyType);
            final var pythonElementType = templatePythonType(map.elementType);

            definitions.mapFunctions.add(new TemplateMapFunction(
                    type, typeSuffix, keyTypeSuffix, elementTypeSuffix,
                    dsmType,
                    pythonType, pythonKeyType, pythonElementType));
        }

        for (var xarray : xarrayFunctions.values()) {
            final var type = convertType(xarray);
            final var typeSuffix = typeSuffix(xarray);
            final var elementType = convertType(xarray.elementType);
            final var elementTypeSuffix = typeSuffix(xarray.elementType);
            final var dsmType = xarray.representation();
            final var pythonType = templatePythonType(xarray);
            final var pythonElementType = templatePythonType(xarray.elementType);

            definitions.xarrayFunctions.add(new TemplateXArrayFunction(type, typeSuffix, elementType, elementTypeSuffix,
                    dsmType,
                    pythonType, pythonElementType));
        }

        for (var variant : variantFunctions.values()) {
            final var type = convertType(variant);
            final var typeSuffix = typeSuffix(variant);
            final var members = new ArrayList<TemplateType>();
            final var dsmType = variant.representation();
            final var pythonType = templatePythonType(variant);
            final var pythonMembers = new ArrayList<TemplatePythonType>();

            for (var memberType : variant.types) {
                final var mType = convertType(memberType);
                final var mTypeSuffix = typeSuffix(memberType);
                members.add(new TemplateType(mType, mTypeSuffix));
                pythonMembers.add(templatePythonType(memberType));
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
            final var key = typeSuffix(typeVec);
            vecFunctions.put(key, typeVec);
            registerFunctionForContainer(typeVec.elementType);

        } else if (type instanceof DSMTypeMat typeMat) {
            final var key = typeSuffix(typeMat);
            matFunctions.put(key, typeMat);
            registerFunctionForContainer(typeMat.elementType);

        } else if (type instanceof DSMTypeTuple typeTuple) {
            final var key = typeSuffix(typeTuple);
            tupleFunctions.put(key, typeTuple);
            for (var memberType : typeTuple.types)
                registerFunctionForContainer(memberType);

        } else if (type instanceof DSMTypeOptional typeOptional) {
            final var key = typeSuffix(typeOptional);
            optionalFunctions.put(key, typeOptional);
            registerFunctionForContainer(typeOptional.elementType);

        } else if (type instanceof DSMTypeVector typeVector) {
            final var key = typeSuffix(typeVector);
            vectorFunctions.put(key, typeVector);
            registerFunctionForContainer(typeVector.elementType);

        } else if (type instanceof DSMTypeSet typeSet) {
            final var key = typeSuffix(typeSet);
            setFunctions.put(key, typeSet);
            registerFunctionForContainer(typeSet.elementType);

        } else if (type instanceof DSMTypeMap typeMap) {
            final var key = typeSuffix(typeMap);
            mapFunctions.put(key, typeMap);
            registerFunctionForContainer(typeMap.keyType);
            registerFunctionForContainer(typeMap.elementType);
            registerFunctionForContainer(new DSMTypeSet(typeMap.keyType));

        } else if (type instanceof DSMTypeXArray typeXArray) {
            final var key = typeSuffix(typeXArray);
            xarrayFunctions.put(key, typeXArray);
            registerFunctionForContainer(typeXArray.elementType);
            registerFunctionForContainer(new DSMTypeVector(typeXArray.elementType));

        } else if (type instanceof DSMTypeVariant typeVariant) {
            final var key = typeSuffix(typeVariant);
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
    private boolean isBlobId(TypeName typeName) {
        return typeName.name.equals(DSMLexicon.BlobId);
    }

    private boolean isCommitId(TypeName typeName) {
        return typeName.name.equals(DSMLexicon.CommitId);
    }

    private boolean isUUId(TypeName typeName) {
        return typeName.name.equals(DSMLexicon.UUId);
    }

    private boolean isString(TypeName typeName) {
        return typeName.name.equals(DSMLexicon.String);
    }

    private boolean isBlob(TypeName typeName) {
        return typeName.name.equals(DSMLexicon.Blob);
    }

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
        final var type = convertType(function.prototype.returnType);
        final var typeSuffix = typeSuffix(function.prototype.returnType);
        final var returnViperValue = viperValue(function.prototype.returnType);
        final var returnPythonType = templatePythonType(function.prototype.returnType);
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
        final var type = convertType(function.prototype.returnType);
        final var typeSuffix = typeSuffix(function.prototype.returnType);
        final var returnViperValue = viperValue(function.prototype.returnType);
        final var templatePythonType = templatePythonType(function.prototype.returnType);
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
        final var type = convertType(parameter.type);
        final var passBy = passByQualifier(parameter.type);
        final var typeSuffix = typeSuffix(parameter.type);
        final var viperValue = viperValue(parameter.type);
        final var templatePythonType = templatePythonType(parameter.type);

        return new TemplateFunctionParameter(parameter, type, passBy, typeSuffix, viperValue, templatePythonType);
    }

    String viperValue(DSMType type) throws Exception {
        if (type instanceof DSMTypeKey typeId)
            return String.format(viperValue(typeId.elementType));

        if (type instanceof DSMTypeVec)
            return "ValueVec";

        if (type instanceof DSMTypeMat)
            return "ValueMat";

        if (type instanceof DSMTypeTuple)
            return "ValueTuple";

        if (type instanceof DSMTypeOptional)
            return "ValueOptional";

        if (type instanceof DSMTypeVector)
            return "ValueVector";

        if (type instanceof DSMTypeSet)
            return "ValueSet";

        if (type instanceof DSMTypeXArray)
            return "ValueXArray";

        if (type instanceof DSMTypeMap)
            return "ValueMap";

        if (type instanceof DSMTypeVariant)
            return "ValueVariant";

        if (type instanceof DSMTypeReference typeReference) {
            return switch (typeReference.domain) {
                case PRIMITIVE -> viperPrimitiveValue(typeReference.typeName.name);
                case CONCEPT, CLUB, ANY_CONCEPT -> "ValueKey";
                case ENUMERATION -> "ValueEnumeration";
                case STRUCTURE -> "ValueStructure";
                case ANY -> "ValueAny";
            };
        }

        throw new ConvertException(String.format("viperValue: type '%s' is not handled.", type.getClass().getName()));
    }

    TemplateField createTemplateField(DSMType type) throws Exception {
        var ctype = TemplateFieldType.BOX;
        var keyType = "<None>";
        var keyTypeSuffix = "<None>";
        var elementType = "<None>";
        var elementTypeSuffix = "<None>";
        var elementTypeViperValue = "<None>";
        TemplatePythonType pythonKeyType = null;
        TemplatePythonType pythonElementType = null;
        var passBy = "<None>";

        if (type instanceof DSMTypeSet typeSet) {
            ctype = TemplateFieldType.SET;
            elementType = convertType(typeSet.elementType);
            elementTypeSuffix = typeSuffix(typeSet.elementType);
            elementTypeViperValue = viperValue(typeSet.elementType);
            pythonElementType = templatePythonType(typeSet.elementType);
            passBy = passByQualifier(typeSet.elementType);
        }

        if (type instanceof DSMTypeMap typeMap) {
            ctype = TemplateFieldType.MAP;
            keyType = convertType(typeMap.keyType);
            keyTypeSuffix = typeSuffix(typeMap.keyType);
            elementType = convertType(typeMap.elementType);
            elementTypeSuffix = typeSuffix(typeMap.elementType);
            elementTypeViperValue = viperValue(typeMap.elementType);
            pythonKeyType = templatePythonType(typeMap.keyType);
            pythonElementType = templatePythonType(typeMap.elementType);
            passBy = passByQualifier(typeMap.elementType);
        }

        if (type instanceof DSMTypeXArray typeXArray) {
            ctype = TemplateFieldType.XARRAY;
            elementType = convertType(typeXArray.elementType);
            elementTypeSuffix = typeSuffix(typeXArray.elementType);
            elementTypeViperValue = viperValue(typeXArray.elementType);
            pythonElementType = templatePythonType(typeXArray.elementType);
            passBy = passByQualifier(typeXArray.elementType);
        }

        return new TemplateField(ctype, keyType, keyTypeSuffix, elementType, elementTypeSuffix, elementTypeViperValue, pythonKeyType, pythonElementType, passBy);
    }

    private TemplatePythonType templatePythonType(DSMType type) throws Exception {
        final var pythonType = pythonType(type);
        final var typeSuffix = typeSuffix(type);

        return new TemplatePythonType(pythonType, typeSuffix);
    }

    private String pythonType(DSMType type) throws Exception {
        if (type instanceof DSMTypeKey typeKey) {
            return pythonType(typeKey.elementType);
        }

        if (type instanceof DSMTypeVec typeVec)
            return String.format("Vec_%s_%d", pythonType(typeVec.elementType), typeVec.size);

        if (type instanceof DSMTypeMat typeMat)
            return String.format("Mat_%s_%d_%d", pythonType(typeMat.elementType), typeMat.columns, typeMat.rows);

        if (type instanceof DSMTypeTuple typeTuple) {
            var memberSuffixes = new ArrayList<String>();
            for (var memberType : typeTuple.types)
                memberSuffixes.add(pythonType(memberType));

            return "Tuple_" + String.join("_", memberSuffixes);
        }

        if (type instanceof DSMTypeOptional typeOptional)
            return String.format("Optional_%s", pythonType(typeOptional.elementType));

        if (type instanceof DSMTypeVector typeVector)
            return String.format("Vector_%s", pythonType(typeVector.elementType));

        if (type instanceof DSMTypeMap typeMap)
            return String.format("Map_%s_to_%s", pythonType(typeMap.keyType), pythonType(typeMap.elementType));

        if (type instanceof DSMTypeSet typeSet)
            return String.format("Set_%s", pythonType(typeSet.elementType));

        if (type instanceof DSMTypeXArray typeXArray)
            return String.format("XArray_%s", pythonType(typeXArray.elementType));

        if (type instanceof DSMTypeVariant typeVariant) {
            var memberSuffixes = new ArrayList<String>();
            for (var memberType : typeVariant.types)
                memberSuffixes.add(pythonType(memberType));

            return "Variant_" + String.join("_", memberSuffixes);
        }

        if (type instanceof DSMTypeReference typeReference) {
            switch (typeReference.domain) {
                case PRIMITIVE -> {
                    return typeReference.typeName.name;
                }
                case ENUMERATION, STRUCTURE -> {
                    return typeReference.typeName.nameSpace.name + "_" + typeReference.typeName.name;
                }
                case CONCEPT, CLUB -> {
                    return typeReference.typeName.nameSpace.name + "_" + typeReference.typeName.name + "Key";
                }
                case ANY_CONCEPT -> {
                    return "AnyConceptKey";
                }
                case ANY -> {
                    return "Any";
                }
            }
        }

        throw new ConvertException(String.format("pythonType: Type '%s' not handled.", type.representation()));
    }

    boolean useBlobId(DSMType type) throws Exception {
        if (type instanceof DSMTypeKey)
            return false;

        if (type instanceof DSMTypeVec)
            return false;

        if (type instanceof DSMTypeMat)
            return false;

        if (type instanceof DSMTypeTuple typeTuple) {
            for (var memberType : typeTuple.types)
                if (useBlobId(memberType))
                    return true;

            return false;
        }

        if (type instanceof DSMTypeOptional typeOptional)
            return useBlobId(typeOptional.elementType);

        if (type instanceof DSMTypeVector typeVector)
            return useBlobId(typeVector.elementType);

        if (type instanceof DSMTypeSet typeSet)
            return useBlobId(typeSet.elementType);

        if (type instanceof DSMTypeMap typeMap) {
            if (useBlobId(typeMap.keyType))
                return true;

            return useBlobId(typeMap.elementType);
        }

        if (type instanceof DSMTypeVariant typeVariant) {
            for (var memberType : typeVariant.types)
                if (useBlobId(memberType))
                    return true;

            return false;
        }

        if (type instanceof DSMTypeXArray typeXArray)
            return useBlobId(typeXArray.elementType);

        if (type instanceof DSMTypeReference typeReference) {
            switch (typeReference.domain) {
                case ANY -> {
                    return true;
                }
                case PRIMITIVE -> {
                    return typeReference.typeName.name.equals(DSMLexicon.BlobId);
                }
                case ENUMERATION, CONCEPT, CLUB, ANY_CONCEPT -> {
                    return false;
                }
                case STRUCTURE -> {
                    final var structure = structuresByTypeName.get(typeReference.typeName);
                    for (var field : structure.fields)
                        if (useBlobId(field.type))
                            return true;

                    return false;
                }
            }
        }

        throw new ConvertException(String.format("useBlobId: type '%s' is not handled.", type.getClass().getName()));
    }

    boolean isStructureMovable(DSMStructure structure) {
        for (var field : structure.fields)
            if (isTypeMovable(field.type))
                return true;

        return false;
    }

    boolean isTypeTupleMovable(DSMTypeTuple typeTuple) {
        for (var type : typeTuple.types)
            if (isTypeMovable(type))
                return true;

        return false;
    }

    boolean isTypeVariantMovable(DSMTypeVariant typeVariant) {
        for (var type : typeVariant.types)
            if (isTypeMovable(type))
                return true;

        return false;
    }

    boolean isTypeMovable(DSMType type) {
        if (type instanceof DSMTypeKey)
            return false;

        if (type instanceof DSMTypeVec)
            return false;

        if (type instanceof DSMTypeMat)
            return false;

        if (type instanceof DSMTypeOptional dsmTypeOptional)
            return isTypeMovable(dsmTypeOptional.elementType);

        if (type instanceof DSMTypeTuple typeTuple)
            return isTypeTupleMovable(typeTuple);

        if (type instanceof DSMTypeVector)
            return true;

        if (type instanceof DSMTypeSet)
            return true;

        if (type instanceof DSMTypeMap)
            return true;

        if (type instanceof DSMTypeVariant typeVariant)
            return isTypeVariantMovable(typeVariant);

        if (type instanceof DSMTypeXArray)
            return true;

        if (type instanceof DSMTypeReference typeReference) {
            switch (typeReference.domain) {
                case ANY, CONCEPT, CLUB, ANY_CONCEPT -> {
                    return true;
                }
                case PRIMITIVE -> {
                    return isBlob(typeReference.typeName) || isString(typeReference.typeName);
                }
                case ENUMERATION -> {
                    return false;
                }
                case STRUCTURE -> {
                    final var structure = structuresByTypeName.get(typeReference.typeName);
                    return isStructureMovable(structure);
                }
            }
        }

        return false;
    }
}




