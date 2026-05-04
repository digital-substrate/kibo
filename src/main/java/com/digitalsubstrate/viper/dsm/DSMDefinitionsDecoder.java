package com.digitalsubstrate.viper.dsm;

import com.digitalsubstrate.viper.stream.StreamDecoding;
import com.digitalsubstrate.viper.stream.StreamTokenBinaryDecoder;
import com.digitalsubstrate.viper.NameSpace;
import com.digitalsubstrate.viper.TypeName;

import java.util.ArrayList;

public final class DSMDefinitionsDecoder {

    private final StreamDecoding decoder;

    public DSMDefinitionsDecoder(byte[] data) {
        this.decoder = new StreamTokenBinaryDecoder(data);
    }

    public DSMDefinitions decode() throws Exception {
        return readDefinitions();
    }

    private DSMDefinitions readDefinitions() throws Exception {
        final var result = new DSMDefinitions();

        result.concepts = readConcepts();
        result.clubs = readClubs();
        result.enumerations = readEnumerations();
        result.structures = readStructures();
        result.attachments = readAttachments();

        result.functionPools = readFunctionPools();
        result.attachmentFunctionPools = readAttachmentFunctionPools();

        return result;
    }

    // Concept
    private ArrayList<DSMConcept> readConcepts() throws Exception {
        final var count = decoder.readUInt64();
        final var result = new ArrayList<DSMConcept>();
        for (var i = 0; i < count; ++i) {
            result.add(readConcept());
        }
        return result;
    }

    private DSMConcept readConcept() throws Exception {
        final var namespaceUUID = decoder.readUuid();
        final var namespaceName = decoder.readString();
        final var name = decoder.readString();

        DSMTypeReference parent = null;
        if (decoder.readBool())
            parent = readOneTypeReference();

        final var documentation = decoder.readString();
        final var runtimeId = decoder.readUuid();

        final var typeName = new TypeName(new NameSpace(namespaceUUID, namespaceName), name);
        return new DSMConcept(typeName, parent, documentation, runtimeId);
    }

    // Club
    private ArrayList<DSMClub> readClubs() throws Exception {
        final var count = decoder.readUInt64();
        final var result = new ArrayList<DSMClub>();
        for (var i = 0; i < count; ++i) {
            result.add(readClub());
        }
        return result;
    }

    private DSMClub readClub() throws Exception {
        final var namespaceUUID = decoder.readUuid();
        final var namespaceName = decoder.readString();
        final var name = decoder.readString();

        final var members = readReferences();
        final var documentation = decoder.readString();
        final var runtimeId = decoder.readUuid();

        final var typeName = new TypeName(new NameSpace(namespaceUUID, namespaceName), name);
        return new DSMClub(typeName, members, documentation, runtimeId);
    }

    // Enumeration
    ArrayList<DSMEnumeration> readEnumerations() throws Exception {
        final var count = decoder.readUInt64();
        final var result = new ArrayList<DSMEnumeration>();
        for (var i = 0; i < count; ++i) {
            result.add(readEnumeration());
        }
        return result;
    }

    DSMEnumeration readEnumeration() throws Exception {
        final var namespaceUUID = decoder.readUuid();
        final var namespaceName = decoder.readString();
        final var name = decoder.readString();

        final var documentation = decoder.readString();
        final var members = readEnumerationCases();
        final var runtimeId = decoder.readUuid();

        final var typeName = new TypeName(new NameSpace(namespaceUUID, namespaceName), name);
        return new DSMEnumeration(typeName, members, documentation, runtimeId);
    }

    ArrayList<DSMEnumerationCase> readEnumerationCases() throws Exception {
        final var count = decoder.readUInt64();
        final var result = new ArrayList<DSMEnumerationCase>();
        for (var i = 0; i < count; ++i) {
            result.add(readEnumerationCase());
        }
        return result;
    }

    DSMEnumerationCase readEnumerationCase() throws Exception {
        final var symbol = decoder.readString();
        final var documentation = decoder.readString();

        return new DSMEnumerationCase(symbol, documentation);
    }

    // Structure
    private ArrayList<DSMStructure> readStructures() throws Exception {
        final var count = decoder.readUInt64();
        final var result = new ArrayList<DSMStructure>();
        for (var i = 0; i < count; ++i) {
            result.add(readStructure());
        }
        return result;
    }

    private DSMStructure readStructure() throws Exception {
        final var namespaceUUID = decoder.readUuid();
        final var namespaceName = decoder.readString();
        final var name = decoder.readString();

        final var documentation = decoder.readString();
        final var fields = readFields();
        final var runtimeId = decoder.readUuid();

        final var typeName = new TypeName(new NameSpace(namespaceUUID, namespaceName), name);
        return new DSMStructure(typeName, fields, documentation, runtimeId);
    }

    private ArrayList<DSMStructureField> readFields() throws Exception {
        final var count = decoder.readUInt64();
        final var result = new ArrayList<DSMStructureField>();
        for (var i = 0; i < count; ++i) {
            result.add(readField());
        }
        return result;
    }

    private DSMStructureField readField() throws Exception {
        final var name = decoder.readString();
        final var type = readType();
        final var defaultValue = readLiteral();
        final var documentation = decoder.readString();

        return new DSMStructureField(name, type, defaultValue, documentation);
    }

    // Attachment
    private ArrayList<DSMAttachment> readAttachments() throws Exception {
        final var count = decoder.readUInt64();
        final var result = new ArrayList<DSMAttachment>();
        for (var i = 0; i < count; ++i) {
            result.add(readAttachment());
        }
        return result;
    }

    private DSMAttachment readAttachment() throws Exception {
        final var namespaceUUID = decoder.readUuid();
        final var namespaceName = decoder.readString();
        final var name = decoder.readString();

        final var keyType = readOneTypeReference();
        final var documentType = readType();

        final var documentation = decoder.readString();
        final var runtimeId = decoder.readUuid();

        final var typeName = new TypeName(new NameSpace(namespaceUUID, namespaceName), name);
        return new DSMAttachment(typeName, keyType, documentType, documentation, runtimeId);
    }

    // FunctionPool
    private ArrayList<DSMFunctionPool> readFunctionPools() throws Exception {
        final var result = new ArrayList<DSMFunctionPool>();
        final var count = decoder.readUInt64();
        for (var i = 0; i < count; ++i) {
            result.add(readFunctionPool());
        }
        return result;
    }

    private DSMFunctionPool readFunctionPool() throws Exception {
        final var uuid = decoder.readUuid();
        final var name = decoder.readString();
        final var functions = readFunctions();
        final var documentation = decoder.readString();

        return new DSMFunctionPool(uuid, name, functions, documentation);
    }

    private ArrayList<DSMFunction> readFunctions() throws Exception {
        final var result = new ArrayList<DSMFunction>();
        final var count = decoder.readUInt64();
        for (var i = 0; i < count; ++i) {
            result.add(readFunction());
        }
        return result;
    }

    private DSMFunction readFunction() throws Exception {
        final var prototype = readFunctionPrototype();
        final var documentation = decoder.readString();
        return new DSMFunction(prototype, documentation);
    }


    // AttachmentFunctionPool
    private ArrayList<DSMAttachmentFunctionPool> readAttachmentFunctionPools() throws Exception {
        final var result = new ArrayList<DSMAttachmentFunctionPool>();
        final var count = decoder.readUInt64();
        for (var i = 0; i < count; ++i) {
            result.add(readAttachmentFunctionPool());
        }
        return result;
    }

    private DSMAttachmentFunctionPool readAttachmentFunctionPool() throws Exception {
        final var uuid = decoder.readUuid();
        final var name = decoder.readString();
        final var functions = readAttachmentFunctions();
        final var documentation = decoder.readString();

        return new DSMAttachmentFunctionPool(uuid, name, functions, documentation);
    }

    private ArrayList<DSMAttachmentFunction> readAttachmentFunctions() throws Exception {
        final var result = new ArrayList<DSMAttachmentFunction>();
        final var count = decoder.readUInt64();
        for (var i = 0; i < count; ++i) {
            result.add(readAttachmentFunction());
        }
        return result;
    }

    private DSMAttachmentFunction readAttachmentFunction() throws Exception {
        final var isMutable = decoder.readBool();
        final var prototype = readFunctionPrototype();
        final var documentation = decoder.readString();
        return new DSMAttachmentFunction(isMutable, prototype, documentation);
    }

    // Function Prototype
    DSMFunctionPrototype readFunctionPrototype() throws Exception {
        final var name = decoder.readString();
        final var parameters = new ArrayList<DSMFunctionPrototypeParameter>();

        final var count = decoder.readUInt64();
        for (var i = 0; i < count; ++i) {
            final var pName = decoder.readString();
            final var pType = readType();
            parameters.add(new DSMFunctionPrototypeParameter(pName, pType));
        }

        final var returnType = readType();
        return new DSMFunctionPrototype(name, parameters, returnType);
    }

    // Type
    private DSMType readType() throws Exception {
        final var rawValue = decoder.readUInt8();

        if (rawValue == DSMDefinitionsCodecLexicon.DSMType.Key)
            return readTypeKey();

        if (rawValue == DSMDefinitionsCodecLexicon.DSMType.Vec)
            return readTypeVec();

        if (rawValue == DSMDefinitionsCodecLexicon.DSMType.Mat)
            return readTypeMat();

        if (rawValue == DSMDefinitionsCodecLexicon.DSMType.Tuple)
            return readTypeTuple();

        if (rawValue == DSMDefinitionsCodecLexicon.DSMType.Optional)
            return readTypeOptional();

        if (rawValue == DSMDefinitionsCodecLexicon.DSMType.Vector)
            return readTypeVector();

        if (rawValue == DSMDefinitionsCodecLexicon.DSMType.Set)
            return readTypeSet();

        if (rawValue == DSMDefinitionsCodecLexicon.DSMType.Map)
            return readTypeMap();

        if (rawValue == DSMDefinitionsCodecLexicon.DSMType.Variant)
            return readTypeVariant();

        if (rawValue == DSMDefinitionsCodecLexicon.DSMType.XArray)
            return readTypeXArray();

        if (rawValue == DSMDefinitionsCodecLexicon.DSMType.Reference)
            return readTypeReference();

        throw new DSMErrorsUnhandledType("readTypeBase");
    }

    private DSMTypeOptional readTypeOptional() throws Exception {
        final var type = readType();
        return new DSMTypeOptional(type);
    }

    private DSMTypeVec readTypeVec() throws Exception {
        final var typeReference = readOneTypeReference();
        final var size = decoder.readUInt64();
        return new DSMTypeVec(typeReference, size);
    }

    private DSMTypeMat readTypeMat() throws Exception {
        final var typeReference = readOneTypeReference();
        final var columns = decoder.readUInt64();
        final var rows = decoder.readUInt64();
        return new DSMTypeMat(typeReference, columns, rows);
    }

    private DSMTypeTuple readTypeTuple() throws Exception {
        final var types = new ArrayList<DSMType>();

        final var count = decoder.readUInt64();
        for (var i = 0; i < count; ++i) {
            final var type = readType();
            types.add(type);
        }
        return new DSMTypeTuple(types);
    }

    private DSMTypeVector readTypeVector() throws Exception {
        final var type = readType();
        return new DSMTypeVector(type);
    }

    private DSMTypeSet readTypeSet() throws Exception {
        final var type = readType();
        return new DSMTypeSet(type);
    }

    private DSMTypeMap readTypeMap() throws Exception {
        final var keyType = readType();
        final var valueType = readType();
        return new DSMTypeMap(keyType, valueType);
    }

    private DSMTypeVariant readTypeVariant() throws Exception {
        final var types = new ArrayList<DSMType>();

        final var count = decoder.readUInt64();
        for (var i = 0; i < count; ++i) {
            var type = readType();
            types.add(type);
        }
        return new DSMTypeVariant(types);
    }

    private DSMTypeXArray readTypeXArray() throws Exception {
        final var type = readType();
        return new DSMTypeXArray(type);
    }

    private DSMTypeKey readTypeKey() throws Exception {
        final var type = readOneTypeReference();
        return new DSMTypeKey(type);
    }

    private DSMTypeReference readOneTypeReference() throws Exception {
        final var rawValue = decoder.readUInt8();
        if (rawValue != DSMDefinitionsCodecLexicon.DSMType.Reference) {
            throw new DSMErrorsNotAReference();
        }
        return readTypeReference();
    }

    private DSMTypeReference readTypeReference() throws Exception {
        final var namespaceUUID = decoder.readUuid();
        final var namespaceName = decoder.readString();
        final var name = decoder.readString();
        final var domain = readReferenceDomain();

        if (domain == DSMTypeReferenceDomain.PRIMITIVE) {
            return DSMTypeReference.byName(name);
        }

        final var typeName = new TypeName(new NameSpace(namespaceUUID, namespaceName), name);
        return new DSMTypeReference(typeName, domain);
    }

    private DSMTypeReferenceDomain readReferenceDomain() throws Exception {
        final var rawValue = decoder.readUInt8();

        if (rawValue == DSMDefinitionsCodecLexicon.ReferenceDomain.Any)
            return DSMTypeReferenceDomain.ANY;

        if (rawValue == DSMDefinitionsCodecLexicon.ReferenceDomain.Primitive)
            return DSMTypeReferenceDomain.PRIMITIVE;

        if (rawValue == DSMDefinitionsCodecLexicon.ReferenceDomain.Concept)
            return DSMTypeReferenceDomain.CONCEPT;

        if (rawValue == DSMDefinitionsCodecLexicon.ReferenceDomain.Club)
            return DSMTypeReferenceDomain.CLUB;

        if (rawValue == DSMDefinitionsCodecLexicon.ReferenceDomain.AnyConcept)
            return DSMTypeReferenceDomain.ANY_CONCEPT;

        if (rawValue == DSMDefinitionsCodecLexicon.ReferenceDomain.Enumeration)
            return DSMTypeReferenceDomain.ENUMERATION;

        if (rawValue == DSMDefinitionsCodecLexicon.ReferenceDomain.Structure)
            return DSMTypeReferenceDomain.STRUCTURE;

        throw new DSMErrorsUnknownValue("ViperDSMTypeReferenceDomain");
    }

    private ArrayList<DSMTypeReference> readReferences() throws Exception {
        final var count = decoder.readUInt64();
        final var result = new ArrayList<DSMTypeReference>();
        for (var i = 0; i < count; ++i) {
            result.add(readOneTypeReference());
        }
        return result;
    }

    // Literal
    private DSMLiteral readLiteral() throws Exception {
        final var rawValue = decoder.readUInt8();

        if (rawValue == DSMDefinitionsCodecLexicon.Literal.Value)
            return readLiteralValue();

        if (rawValue == DSMDefinitionsCodecLexicon.Literal.List)
            return readLiteralList();

        throw new DSMErrorsUnhandledType("readLiteralBase");
    }

    private DSMLiteralValue readLiteralValue() throws Exception {
        final var domain = readLiteralDomain();
        final var value = decoder.readString();
        return new DSMLiteralValue(domain, value);
    }

    private DSMLiteralDomain readLiteralDomain() throws Exception {
        final var rawValue = decoder.readUInt8();

        if (rawValue == DSMDefinitionsCodecLexicon.LiteralDomain.None)
            return DSMLiteralDomain.NONE;

        if (rawValue == DSMDefinitionsCodecLexicon.LiteralDomain.Boolean)
            return DSMLiteralDomain.BOOLEAN;

        if (rawValue == DSMDefinitionsCodecLexicon.LiteralDomain.Integer)
            return DSMLiteralDomain.INTEGER;

        if (rawValue == DSMDefinitionsCodecLexicon.LiteralDomain.Float)
            return DSMLiteralDomain.FLOAT;

        if (rawValue == DSMDefinitionsCodecLexicon.LiteralDomain.Double)
            return DSMLiteralDomain.DOUBLE;

        if (rawValue == DSMDefinitionsCodecLexicon.LiteralDomain.String)
            return DSMLiteralDomain.STRING;

        if (rawValue == DSMDefinitionsCodecLexicon.LiteralDomain.Uuid)
            return DSMLiteralDomain.UUID;

        if (rawValue == DSMDefinitionsCodecLexicon.LiteralDomain.EnumerationCase)
            return DSMLiteralDomain.ENUMERATION_CASE;

        throw new DSMErrorsUnhandledType("readLiteralDomain");
    }

    private DSMLiteralList readLiteralList() throws Exception {
        final var members = readLiterals();
        return new DSMLiteralList(members);
    }

    private ArrayList<DSMLiteral> readLiterals() throws Exception {
        final var result = new ArrayList<DSMLiteral>();
        final var count = decoder.readUInt64();
        for (var i = 0; i < count; ++i) {
            result.add(readLiteral());
        }
        return result;
    }
}
