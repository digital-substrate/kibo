package com.digitalsubstrate.converter;

import com.digitalsubstrate.template.*;
import com.digitalsubstrate.viper.NameSpace;
import com.digitalsubstrate.viper.TypeName;
import com.digitalsubstrate.viper.dsm.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

final class EntityConverter {
    final HashMap<TypeName, TemplateConcept> templateConceptByTypeName = new HashMap<>();
    final HashMap<TypeName, TemplateStructure> templateStructureByTypeName = new HashMap<>();
    private final HashMap<TypeName, ArrayList<TemplateAttachment>> attachmentByConcept = new HashMap<>();

    private final DSMDefinitions definitions;
    private final DSMStructureDependency structureDependency;
    private final TypeConverter typeConverter;
    private final LiteralConverter literalConverter;
    private final FunctionRegistrar functionRegistrar;

    EntityConverter(DSMDefinitions definitions,
                    DSMStructureDependency structureDependency,
                    TypeConverter typeConverter,
                    LiteralConverter literalConverter,
                    FunctionRegistrar functionRegistrar) {
        this.definitions = definitions;
        this.structureDependency = structureDependency;
        this.typeConverter = typeConverter;
        this.literalConverter = literalConverter;
        this.functionRegistrar = functionRegistrar;
    }

    ArrayList<TemplateEnumeration> convertEnumerations() {
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

    ArrayList<TemplateStructure> convertStructures() throws Exception {
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

    ArrayList<TemplateConcept> collectConceptChildren(DSMConcept concept) {
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

    ArrayList<TemplateConcept> convertConcepts() throws Exception {
        final var result = new ArrayList<TemplateConcept>();
        for (var concept : definitions.concepts) {
            functionRegistrar.registerFunctionForContainer(new DSMTypeOptional(concept.typeReference));
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

    ArrayList<TemplateClub> convertClubs() throws Exception {
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
            functionRegistrar.registerFunctionForContainer(new DSMTypeOptional(club.typeReference));
            final var templateConcept = templateConceptByTypeName.get(member.typeName);
            assert templateConcept != null;
            members.add(templateConcept);
        }
        members.sort(Comparator.comparing(TemplateConcept::getName));

        return new TemplateClub(club, type, typeSuffix, members);
    }

    ArrayList<TemplateAttachment> convertAttachments(ArrayList<DSMAttachment> attachments) throws Exception {
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
        functionRegistrar.registerFunctionForContainer(new DSMTypeSet(dsmKeyType));
        final var type = typeConverter.convertType(dsmKeyType);
        final var typeInNamespace = typeConverter.convertTypeInNamespace(nameSpace, dsmKeyType);
        final var typeSuffix = typeConverter.typeSuffix(dsmKeyType);
        final var viperValue = typeConverter.viperValue(dsmKeyType);
        final var pythonType = typeConverter.templatePythonType(dsmKeyType);

        return new TemplateAttachedKeyType(dsmKeyType.typeName, type, typeInNamespace, typeSuffix, viperValue, pythonType);
    }

    private TemplateAttachedDocumentType convertAttachmentDocumentType(NameSpace nameSpace, DSMType dsmDocumentType) throws Exception {
        functionRegistrar.registerFunctionForContainer(new DSMTypeOptional(dsmDocumentType));

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
}
