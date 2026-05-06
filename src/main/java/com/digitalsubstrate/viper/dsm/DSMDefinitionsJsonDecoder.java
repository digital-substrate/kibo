package com.digitalsubstrate.viper.dsm;

import com.digitalsubstrate.viper.NameSpace;
import com.digitalsubstrate.viper.TypeName;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.UUID;

import static com.digitalsubstrate.viper.dsm.DSMDefinitionsJsonLexicon.*;

// Mirror of viper's Viper_JsonDSMDefinitionsDecoder.cpp. Walks the Jackson
// tree, tracks the JSON path so errors quote the failing node by location
// (e.g. clubs[2].runtime_id) instead of just its kind.
public final class DSMDefinitionsJsonDecoder {

    private final byte[] data;
    private final Deque<String> path = new ArrayDeque<>();

    public DSMDefinitionsJsonDecoder(byte[] data) {
        this.data = data;
    }

    public DSMDefinitions decode() throws Exception {
        final var mapper = new ObjectMapper();
        final JsonNode root;
        try {
            root = mapper.readTree(data);
        } catch (Exception e) {
            throw new DSMDefinitionsJsonDecodingException("invalid json: " + e.getMessage(), e);
        }
        return decodeDefinitions(root);
    }

    @FunctionalInterface
    private interface DecoderAction<T> {
        T apply() throws Exception;
    }

    private <T> T descend(String segment, DecoderAction<T> action) throws Exception {
        path.addLast(segment);
        try {
            return action.apply();
        } finally {
            path.removeLast();
        }
    }

    private <T> T descendIndex(int i, DecoderAction<T> action) throws Exception {
        return descend("[" + i + "]", action);
    }

    private String pathString() {
        if (path.isEmpty())
            return "$";
        var sb = new StringBuilder();
        for (var seg : path) {
            if (!sb.isEmpty() && seg.charAt(0) != '[')
                sb.append('.');
            sb.append(seg);
        }
        return sb.toString();
    }

    private String pathStringWith(String member) {
        var p = pathString();
        return p.equals("$") ? member : (p + "." + member);
    }

    // MARK: - Top level
    private DSMDefinitions decodeDefinitions(JsonNode node) throws Exception {
        if (!node.isObject())
            throw DSMDefinitionsJsonDecodingException.expectedType(pathString(), "object");

        final var jConcepts = checkMemberArray(node, Concepts);
        final var jClubs = checkMemberArray(node, Clubs);
        final var jEnumerations = checkMemberArray(node, Enumerations);
        final var jStructures = checkMemberArray(node, Structures);
        final var jAttachments = checkMemberArray(node, Attachments);
        final var jFunctionPools = checkMemberArray(node, FunctionPools);
        final var jAttachmentFunctionPools = checkMemberArray(node, AttachmentFunctionPools);

        final var result = new DSMDefinitions();
        result.concepts                = descend(Concepts,                () -> decodeConcepts(jConcepts));
        result.clubs                   = descend(Clubs,                   () -> decodeClubs(jClubs));
        result.enumerations            = descend(Enumerations,            () -> decodeEnumerations(jEnumerations));
        result.structures              = descend(Structures,              () -> decodeStructures(jStructures));
        result.attachments             = descend(Attachments,             () -> decodeAttachments(jAttachments));
        result.functionPools           = descend(FunctionPools,           () -> decodeFunctionPools(jFunctionPools));
        result.attachmentFunctionPools = descend(AttachmentFunctionPools, () -> decodeAttachmentFunctionPools(jAttachmentFunctionPools));
        return result;
    }

    // MARK: - Concept
    private ArrayList<DSMConcept> decodeConcepts(JsonNode node) throws Exception {
        final var result = new ArrayList<DSMConcept>(node.size());
        for (var i = 0; i < node.size(); ++i) {
            final var idx = i;
            result.add(descendIndex(idx, () -> decodeConcept(node.get(idx))));
        }
        return result;
    }

    private DSMConcept decodeConcept(JsonNode node) throws Exception {
        final var typeName = decodeTypeName(node);
        final var documentation = decodeDocumentation(node);

        DSMTypeReference parent = null;
        if (node.has(Parent)) {
            parent = descend(Parent, () -> decodeTypeReference(node.get(Parent)));
        }

        final var runtimeId = decodeRuntimeId(node);
        return new DSMConcept(typeName, parent, documentation, runtimeId);
    }

    // MARK: - Club
    private ArrayList<DSMClub> decodeClubs(JsonNode node) throws Exception {
        final var result = new ArrayList<DSMClub>(node.size());
        for (var i = 0; i < node.size(); ++i) {
            final var idx = i;
            result.add(descendIndex(idx, () -> decodeClub(node.get(idx))));
        }
        return result;
    }

    private DSMClub decodeClub(JsonNode node) throws Exception {
        final var typeName = decodeTypeName(node);
        final var documentation = decodeDocumentation(node);

        final var jMembers = checkMemberArray(node, Members);
        final var members = descend(Members, () -> decodeTypeReferences(jMembers));

        final var runtimeId = decodeRuntimeId(node);
        return new DSMClub(typeName, members, documentation, runtimeId);
    }

    // MARK: - Enumeration
    private ArrayList<DSMEnumeration> decodeEnumerations(JsonNode node) throws Exception {
        final var result = new ArrayList<DSMEnumeration>(node.size());
        for (var i = 0; i < node.size(); ++i) {
            final var idx = i;
            result.add(descendIndex(idx, () -> decodeEnumeration(node.get(idx))));
        }
        return result;
    }

    private DSMEnumeration decodeEnumeration(JsonNode node) throws Exception {
        final var typeName = decodeTypeName(node);
        final var documentation = decodeDocumentation(node);

        final var jMembers = checkMemberArray(node, Members);
        final var members = descend(Members, () -> decodeEnumerationCases(jMembers));

        final var runtimeId = decodeRuntimeId(node);
        return new DSMEnumeration(typeName, members, documentation, runtimeId);
    }

    private ArrayList<DSMEnumerationCase> decodeEnumerationCases(JsonNode node) throws Exception {
        final var result = new ArrayList<DSMEnumerationCase>(node.size());
        for (var i = 0; i < node.size(); ++i) {
            final var idx = i;
            result.add(descendIndex(idx, () -> decodeEnumerationCase(node.get(idx))));
        }
        return result;
    }

    private DSMEnumerationCase decodeEnumerationCase(JsonNode node) throws Exception {
        final var name = decodeName(node);
        final var documentation = decodeDocumentation(node);
        return new DSMEnumerationCase(name, documentation);
    }

    // MARK: - Structure
    private ArrayList<DSMStructure> decodeStructures(JsonNode node) throws Exception {
        final var result = new ArrayList<DSMStructure>(node.size());
        for (var i = 0; i < node.size(); ++i) {
            final var idx = i;
            result.add(descendIndex(idx, () -> decodeStructure(node.get(idx))));
        }
        return result;
    }

    private DSMStructure decodeStructure(JsonNode node) throws Exception {
        final var typeName = decodeTypeName(node);
        final var documentation = decodeDocumentation(node);

        final var jFields = checkMemberArray(node, Fields);
        final var fields = descend(Fields, () -> decodeStructureFields(jFields));

        final var runtimeId = decodeRuntimeId(node);
        return new DSMStructure(typeName, fields, documentation, runtimeId);
    }

    private ArrayList<DSMStructureField> decodeStructureFields(JsonNode node) throws Exception {
        final var result = new ArrayList<DSMStructureField>(node.size());
        for (var i = 0; i < node.size(); ++i) {
            final var idx = i;
            result.add(descendIndex(idx, () -> decodeStructureField(node.get(idx))));
        }
        return result;
    }

    private DSMStructureField decodeStructureField(JsonNode node) throws Exception {
        final var name = decodeName(node);
        final var documentation = decodeDocumentation(node);

        final var jType = checkMemberObject(node, Type);
        final var jDefaultValue = checkMemberObject(node, DefaultValue);

        final var type = descend(Type, () -> decodeType(jType));
        final var defaultValue = descend(DefaultValue, () -> decodeLiteral(jDefaultValue));

        return new DSMStructureField(name, type, defaultValue, documentation);
    }

    // MARK: - Attachment
    private ArrayList<DSMAttachment> decodeAttachments(JsonNode node) throws Exception {
        final var result = new ArrayList<DSMAttachment>(node.size());
        for (var i = 0; i < node.size(); ++i) {
            final var idx = i;
            result.add(descendIndex(idx, () -> decodeAttachment(node.get(idx))));
        }
        return result;
    }

    private DSMAttachment decodeAttachment(JsonNode node) throws Exception {
        final var typeName = decodeTypeName(node);

        final var jKeyType = checkMemberObject(node, KeyType);
        final var keyType = descend(KeyType, () -> decodeTypeReference(jKeyType));

        final var jDocumentType = checkMemberObject(node, DocumentType);
        final var documentType = descend(DocumentType, () -> decodeType(jDocumentType));

        final var documentation = decodeDocumentation(node);
        final var runtimeId = decodeRuntimeId(node);
        return new DSMAttachment(typeName, keyType, documentType, documentation, runtimeId);
    }

    // MARK: - FunctionPool
    private ArrayList<DSMFunctionPool> decodeFunctionPools(JsonNode node) throws Exception {
        final var result = new ArrayList<DSMFunctionPool>(node.size());
        for (var i = 0; i < node.size(); ++i) {
            final var idx = i;
            result.add(descendIndex(idx, () -> decodeFunctionPool(node.get(idx))));
        }
        return result;
    }

    private DSMFunctionPool decodeFunctionPool(JsonNode node) throws Exception {
        final var uuid = decodePoolUUID(node);
        final var name = decodeName(node);
        final var documentation = decodeDocumentation(node);

        final var jFunctions = checkMemberArray(node, Functions);
        final var functions = descend(Functions, () -> decodeFunctions(jFunctions));

        return new DSMFunctionPool(uuid, name, functions, documentation);
    }

    private ArrayList<DSMFunction> decodeFunctions(JsonNode node) throws Exception {
        final var result = new ArrayList<DSMFunction>(node.size());
        for (var i = 0; i < node.size(); ++i) {
            final var idx = i;
            result.add(descendIndex(idx, () -> decodeFunction(node.get(idx))));
        }
        return result;
    }

    private DSMFunction decodeFunction(JsonNode node) throws Exception {
        final var jPrototype = checkMemberObject(node, Prototype);
        final var prototype = descend(Prototype, () -> decodeFunctionPrototype(jPrototype));
        final var documentation = decodeDocumentation(node);
        return new DSMFunction(prototype, documentation);
    }

    // MARK: - AttachmentFunctionPool
    private ArrayList<DSMAttachmentFunctionPool> decodeAttachmentFunctionPools(JsonNode node) throws Exception {
        final var result = new ArrayList<DSMAttachmentFunctionPool>(node.size());
        for (var i = 0; i < node.size(); ++i) {
            final var idx = i;
            result.add(descendIndex(idx, () -> decodeAttachmentFunctionPool(node.get(idx))));
        }
        return result;
    }

    private DSMAttachmentFunctionPool decodeAttachmentFunctionPool(JsonNode node) throws Exception {
        final var uuid = decodePoolUUID(node);
        final var name = decodeName(node);
        final var documentation = decodeDocumentation(node);

        final var jFunctions = checkMemberArray(node, Functions);
        final var functions = descend(Functions, () -> decodeAttachmentFunctions(jFunctions));

        return new DSMAttachmentFunctionPool(uuid, name, functions, documentation);
    }

    private ArrayList<DSMAttachmentFunction> decodeAttachmentFunctions(JsonNode node) throws Exception {
        final var result = new ArrayList<DSMAttachmentFunction>(node.size());
        for (var i = 0; i < node.size(); ++i) {
            final var idx = i;
            result.add(descendIndex(idx, () -> decodeAttachmentFunction(node.get(idx))));
        }
        return result;
    }

    private DSMAttachmentFunction decodeAttachmentFunction(JsonNode node) throws Exception {
        final var isMutable = checkMemberBool(node, IsMutable);

        final var jPrototype = checkMemberObject(node, Prototype);
        final var prototype = descend(Prototype, () -> decodeFunctionPrototype(jPrototype));

        final var documentation = decodeDocumentation(node);
        return new DSMAttachmentFunction(isMutable, prototype, documentation);
    }

    // MARK: - Function Prototype
    private DSMFunctionPrototype decodeFunctionPrototype(JsonNode node) throws Exception {
        final var name = decodeName(node);

        final var jParameters = checkMemberArray(node, Parameters);
        final var parameters = descend(Parameters, () -> decodeFunctionPrototypeParameters(jParameters));

        final var jReturnType = checkMemberObject(node, ReturnType);
        final var returnType = descend(ReturnType, () -> decodeType(jReturnType));

        return new DSMFunctionPrototype(name, parameters, returnType);
    }

    private ArrayList<DSMFunctionPrototypeParameter> decodeFunctionPrototypeParameters(JsonNode node) throws Exception {
        final var result = new ArrayList<DSMFunctionPrototypeParameter>(node.size());
        for (var i = 0; i < node.size(); ++i) {
            final var idx = i;
            result.add(descendIndex(idx, () -> decodeFunctionPrototypeParameter(node.get(idx))));
        }
        return result;
    }

    private DSMFunctionPrototypeParameter decodeFunctionPrototypeParameter(JsonNode node) throws Exception {
        final var name = decodeName(node);
        final var jType = checkMemberObject(node, Type);
        final var type = descend(Type, () -> decodeType(jType));
        return new DSMFunctionPrototypeParameter(name, type);
    }

    // MARK: - Types
    private ArrayList<DSMType> decodeTypes(JsonNode node) throws Exception {
        final var result = new ArrayList<DSMType>(node.size());
        for (var i = 0; i < node.size(); ++i) {
            final var idx = i;
            result.add(descendIndex(idx, () -> decodeType(node.get(idx))));
        }
        return result;
    }

    private DSMType decodeType(JsonNode node) throws Exception {
        final var className = checkMemberString(node, ClassName);

        return switch (className) {
            case TypeKey       -> decodeTypeKey(node);
            case TypeVec       -> decodeTypeVec(node);
            case TypeMat       -> decodeTypeMat(node);
            case TypeTuple     -> decodeTypeTuple(node);
            case TypeOptional  -> decodeTypeOptional(node);
            case TypeVector    -> decodeTypeVector(node);
            case TypeSet       -> decodeTypeSet(node);
            case TypeMap       -> decodeTypeMap(node);
            case TypeXArray    -> decodeTypeXArray(node);
            case TypeVariant   -> decodeTypeVariant(node);
            case TypeReference -> decodeTypeReference(node);
            default -> throw DSMDefinitionsJsonDecodingException.unknownValue(
                    pathStringWith(ClassName), "ClassName", className);
        };
    }

    private DSMTypeKey decodeTypeKey(JsonNode node) throws Exception {
        final var jElementType = checkMember(node, ElementType);
        final var elementType = descend(ElementType, () -> decodeTypeReference(jElementType));
        return new DSMTypeKey(elementType);
    }

    private DSMTypeVec decodeTypeVec(JsonNode node) throws Exception {
        final var jElementType = checkMember(node, ElementType);
        final var size = checkMemberInteger(node, Size);
        final var elementType = descend(ElementType, () -> decodeTypeReference(jElementType));
        return new DSMTypeVec(elementType, size);
    }

    private DSMTypeMat decodeTypeMat(JsonNode node) throws Exception {
        final var jElementType = checkMember(node, ElementType);
        final var columns = checkMemberInteger(node, Columns);
        final var rows = checkMemberInteger(node, Rows);
        final var elementType = descend(ElementType, () -> decodeTypeReference(jElementType));
        return new DSMTypeMat(elementType, columns, rows);
    }

    private DSMTypeTuple decodeTypeTuple(JsonNode node) throws Exception {
        final var jTypes = checkMemberArray(node, Types);
        final var types = descend(Types, () -> decodeTypes(jTypes));
        return new DSMTypeTuple(types);
    }

    private DSMTypeOptional decodeTypeOptional(JsonNode node) throws Exception {
        return new DSMTypeOptional(decodeElementType(node));
    }

    private DSMTypeSet decodeTypeSet(JsonNode node) throws Exception {
        return new DSMTypeSet(decodeElementType(node));
    }

    private DSMTypeVector decodeTypeVector(JsonNode node) throws Exception {
        return new DSMTypeVector(decodeElementType(node));
    }

    private DSMTypeMap decodeTypeMap(JsonNode node) throws Exception {
        final var jKeyType = checkMemberObject(node, KeyType);
        final var keyType = descend(KeyType, () -> decodeType(jKeyType));
        return new DSMTypeMap(keyType, decodeElementType(node));
    }

    private DSMTypeVariant decodeTypeVariant(JsonNode node) throws Exception {
        final var jTypes = checkMemberArray(node, Types);
        final var types = descend(Types, () -> decodeTypes(jTypes));
        return new DSMTypeVariant(types);
    }

    private DSMTypeXArray decodeTypeXArray(JsonNode node) throws Exception {
        return new DSMTypeXArray(decodeElementType(node));
    }

    private ArrayList<DSMTypeReference> decodeTypeReferences(JsonNode node) throws Exception {
        final var result = new ArrayList<DSMTypeReference>(node.size());
        for (var i = 0; i < node.size(); ++i) {
            final var idx = i;
            result.add(descendIndex(idx, () -> decodeTypeReference(node.get(idx))));
        }
        return result;
    }

    private DSMTypeReference decodeTypeReference(JsonNode node) throws Exception {
        final var namespaceUUID = decodeNamespaceUUID(node);
        final var namespaceName = decodeNamespaceName(node);
        final var name = decodeName(node);
        final var jDomain = decodeDomain(node);
        final var domain = decodeTypeReferenceDomain(jDomain);

        if (domain == DSMTypeReferenceDomain.PRIMITIVE)
            return DSMTypeReference.byName(name);

        final var typeName = new TypeName(new NameSpace(namespaceUUID, namespaceName), name);
        return new DSMTypeReference(typeName, domain);
    }

    private DSMTypeReferenceDomain decodeTypeReferenceDomain(String jDomain) throws Exception {
        return switch (jDomain) {
            case Any         -> DSMTypeReferenceDomain.ANY;
            case Primitive   -> DSMTypeReferenceDomain.PRIMITIVE;
            case Concept     -> DSMTypeReferenceDomain.CONCEPT;
            case Club        -> DSMTypeReferenceDomain.CLUB;
            case AnyConcept  -> DSMTypeReferenceDomain.ANY_CONCEPT;
            case Enumeration -> DSMTypeReferenceDomain.ENUMERATION;
            case Structure   -> DSMTypeReferenceDomain.STRUCTURE;
            default -> throw DSMDefinitionsJsonDecodingException.unknownValue(
                    pathStringWith(Domain), "TypeReferenceDomain", jDomain);
        };
    }

    // MARK: - Literal
    private DSMLiteral decodeLiteral(JsonNode node) throws Exception {
        final var className = checkMemberString(node, ClassName);

        return switch (className) {
            case LiteralList  -> decodeLiteralList(node);
            case LiteralValue -> decodeLiteralValue(node);
            default -> throw DSMDefinitionsJsonDecodingException.unknownValue(
                    pathStringWith(ClassName), "ClassName", className);
        };
    }

    private DSMLiteralList decodeLiteralList(JsonNode node) throws Exception {
        final var jMembers = checkMemberArray(node, Members);
        final var members = descend(Members, () -> decodeLiteralListMembers(jMembers));
        return new DSMLiteralList(members);
    }

    private ArrayList<DSMLiteral> decodeLiteralListMembers(JsonNode node) throws Exception {
        final var result = new ArrayList<DSMLiteral>(node.size());
        for (var i = 0; i < node.size(); ++i) {
            final var idx = i;
            result.add(descendIndex(idx, () -> decodeLiteral(node.get(idx))));
        }
        return result;
    }

    private DSMLiteralValue decodeLiteralValue(JsonNode node) throws Exception {
        final var jDomain = decodeDomain(node);
        final var domain = decodeLiteralDomain(jDomain);
        final var value = checkMemberString(node, Value);
        return new DSMLiteralValue(domain, value);
    }

    private DSMLiteralDomain decodeLiteralDomain(String jDomain) throws Exception {
        return switch (jDomain) {
            case None             -> DSMLiteralDomain.NONE;
            case Boolean          -> DSMLiteralDomain.BOOLEAN;
            case Integer          -> DSMLiteralDomain.INTEGER;
            case Float            -> DSMLiteralDomain.FLOAT;
            case Double           -> DSMLiteralDomain.DOUBLE;
            case String           -> DSMLiteralDomain.STRING;
            case UUId             -> DSMLiteralDomain.UUID;
            case EnumerationCase  -> DSMLiteralDomain.ENUMERATION_CASE;
            default -> throw DSMDefinitionsJsonDecodingException.unknownValue(
                    pathStringWith(Domain), "LiteralDomain", jDomain);
        };
    }

    // MARK: - Helpers (typed member readers)
    private TypeName decodeTypeName(JsonNode node) throws Exception {
        final var namespaceUUID = decodeNamespaceUUID(node);
        final var namespaceName = decodeNamespaceName(node);
        final var name = decodeName(node);
        return new TypeName(new NameSpace(namespaceUUID, namespaceName), name);
    }

    private UUID decodeRuntimeId(JsonNode node) throws Exception {
        return UUID.fromString(checkMemberString(node, RuntimeId));
    }

    private UUID decodePoolUUID(JsonNode node) throws Exception {
        return UUID.fromString(checkMemberString(node, UUId));
    }

    private UUID decodeNamespaceUUID(JsonNode node) throws Exception {
        return UUID.fromString(checkMemberString(node, NamespaceUUID));
    }

    private String decodeNamespaceName(JsonNode node) throws Exception {
        return checkMemberString(node, NamespaceName);
    }

    private String decodeName(JsonNode node) throws Exception {
        return checkMemberString(node, Name);
    }

    private String decodeDocumentation(JsonNode node) throws Exception {
        return checkMemberString(node, Documentation);
    }

    private String decodeDomain(JsonNode node) throws Exception {
        return checkMemberString(node, Domain);
    }

    private DSMType decodeElementType(JsonNode node) throws Exception {
        final var jType = checkMemberObject(node, ElementType);
        return descend(ElementType, () -> decodeType(jType));
    }

    // MARK: - Checks
    private JsonNode checkMember(JsonNode node, String member) throws DSMDefinitionsJsonDecodingException {
        if (!node.isObject())
            throw DSMDefinitionsJsonDecodingException.expectedType(pathString(), "object");

        if (!node.has(member))
            throw DSMDefinitionsJsonDecodingException.expectedMember(pathString(), member);

        return node.get(member);
    }

    private JsonNode checkMemberObject(JsonNode node, String member) throws DSMDefinitionsJsonDecodingException {
        final var result = checkMember(node, member);
        if (!result.isObject())
            throw DSMDefinitionsJsonDecodingException.expectedMemberType(pathString(), member, "object");
        return result;
    }

    private JsonNode checkMemberArray(JsonNode node, String member) throws DSMDefinitionsJsonDecodingException {
        final var result = checkMember(node, member);
        if (!result.isArray())
            throw DSMDefinitionsJsonDecodingException.expectedMemberType(pathString(), member, "array");
        return result;
    }

    private String checkMemberString(JsonNode node, String member) throws DSMDefinitionsJsonDecodingException {
        final var result = checkMember(node, member);
        if (!result.isTextual())
            throw DSMDefinitionsJsonDecodingException.expectedMemberType(pathString(), member, "string");
        return result.asText();
    }

    private long checkMemberInteger(JsonNode node, String member) throws DSMDefinitionsJsonDecodingException {
        final var result = checkMember(node, member);
        if (!result.isIntegralNumber())
            throw DSMDefinitionsJsonDecodingException.expectedMemberType(pathString(), member, "integer");
        return result.asLong();
    }

    private boolean checkMemberBool(JsonNode node, String member) throws DSMDefinitionsJsonDecodingException {
        final var result = checkMember(node, member);
        if (!result.isBoolean())
            throw DSMDefinitionsJsonDecodingException.expectedMemberType(pathString(), member, "boolean");
        return result.asBoolean();
    }
}
