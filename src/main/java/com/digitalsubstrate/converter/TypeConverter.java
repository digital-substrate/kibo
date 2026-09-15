package com.digitalsubstrate.converter;

import com.digitalsubstrate.template.TemplateField;
import com.digitalsubstrate.template.TemplateFieldType;
import com.digitalsubstrate.template.TemplateBindingType;
import com.digitalsubstrate.template.TemplateTool;
import com.digitalsubstrate.viper.NameSpace;
import com.digitalsubstrate.viper.TypeName;
import com.digitalsubstrate.viper.dsm.*;

import java.util.ArrayList;
import java.util.HashMap;

final class TypeConverter {
    /** kibo's own proxy name for the ANY domain, which the DSM lexicon does not name. */
    static final String Any = "Any";

    private final HashMap<String, String> cppPrimitiveTypes;
    private final HashMap<String, String> viperPrimitiveValues;
    private final HashMap<TypeName, DSMStructure> structuresByTypeName;
    private final BindingVocabulary vocabulary;

    /**
     * Where the untyped key lives, spelled so that it resolves from anywhere.
     *
     * <p>{@code key<any_concept>} has no namespace of the model behind it, so nothing
     * qualified it and the bare name came out. That resolves inside a file wrapped in the
     * model's namespace and nowhere else — a per-unit file is not, and a bare
     * {@code AnyConceptKey} there names nothing.
     *
     * <p>The class belongs to the model rather than to the runtime, and that is not a
     * fallback. Two models linked into one program would each define a
     * {@code Viper::AnyConceptKey}; one definition per model has no such problem, and the
     * runtime as shipped carries no such type to borrow.
     */
    private final String model;

    TypeConverter(String model,
                  HashMap<String, String> cppPrimitiveTypes,
                  HashMap<String, String> viperPrimitiveValues,
                  HashMap<TypeName, DSMStructure> structuresByTypeName,
                  BindingVocabulary vocabulary) {
        this.model = model;
        this.cppPrimitiveTypes = cppPrimitiveTypes;
        this.viperPrimitiveValues = viperPrimitiveValues;
        this.structuresByTypeName = structuresByTypeName;
        this.vocabulary = vocabulary;
    }

    /**
     * Whether a type needs a generated proxy. The DSM primitives do not — they cross the
     * binding as host values — and everything else does. Shared by every binding; only how
     * those primitives are spelled differs, and that is the vocabulary's business.
     */
    private boolean needsProxy(String proxy) {
        return switch (proxy) {
            case DSMLexicon.Bool,
                 DSMLexicon.UInt8, DSMLexicon.UInt16, DSMLexicon.UInt32, DSMLexicon.UInt64,
                 DSMLexicon.Int8, DSMLexicon.Int16, DSMLexicon.Int32, DSMLexicon.Int64,
                 DSMLexicon.Float, DSMLexicon.Double,
                 DSMLexicon.BlobId, DSMLexicon.CommitId, DSMLexicon.UUId,
                 DSMLexicon.String, DSMLexicon.Blob,
                 DSMLexicon.Void,
                 Any -> false;
            default -> true;
        };
    }

    /** How the target writes a fixed-size sequence, or nothing under a native binding. */
    String bindingSequence(String element, long count) {
        return vocabulary == null || element == null ? null : vocabulary.sequence(element, count);
    }

    boolean isTypeAny(DSMType type) {
        if (type instanceof DSMTypeReference typeReference) {
            return typeReference.domain == DSMTypeReferenceDomain.ANY;
        }

        return false;
    }

    String convertType(DSMType type) throws ConvertException {
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
                    // Qualifiée ici aussi : un pool n'est dans aucun namespace du modèle,
                    // et un nom nu n'y résout rien. Là où le nom nu marchait -- dans un
                    // fichier enveloppé du namespace du modèle -- la forme qualifiée
                    // marche également.
                    return "::" + model + "::AnyConceptKey";
                }
                case ANY -> {
                    return "Viper::Any";
                }
            }
        }

        throw new ConvertException(String.format("convertType: type '%s' is not handled.", type.getClass().getName()));
    }

    String convertTypeInNamespace(NameSpace nameSpace, DSMType type) throws ConvertException {
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
                    // Depuis la portée globale : un namespace du modèle pourrait porter le
                    // nom du modèle, et la recherche s'arrêterait sur lui.
                    return "::" + model + "::AnyConceptKey";
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

    String passByQualifier(DSMType type) {
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

    String typeSuffix(DSMType type) throws Exception {
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

    String typeForKey(TypeName typeName) {
        return String.format("%sKey", typeName.representation());
    }

    String typeSuffixForKey(TypeName typeName) {
        return String.format("_%s_%sKey", typeName.nameSpace.name, typeName.name);
    }

    String typeSuffix(TypeName typeName) {
        if (typeName.nameSpace.isGlobal())
            return String.format("_%s", typeName.name);
        return String.format("_%s_%s", typeName.nameSpace.name, typeName.name);
    }

    String viperPrimitiveValue(String name) throws Exception {
        final var result = viperPrimitiveValues.get(name);
        if (result == null)
            throw new ConvertException(String.format("viperPrimitiveValue: %s is not handled", name));

        return result;
    }

    boolean isBlobId(TypeName typeName) {
        return typeName.name.equals(DSMLexicon.BlobId);
    }

    boolean isCommitId(TypeName typeName) {
        return typeName.name.equals(DSMLexicon.CommitId);
    }

    boolean isUUId(TypeName typeName) {
        return typeName.name.equals(DSMLexicon.UUId);
    }

    boolean isString(TypeName typeName) {
        return typeName.name.equals(DSMLexicon.String);
    }

    boolean isBlob(TypeName typeName) {
        return typeName.name.equals(DSMLexicon.Blob);
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
        TemplateBindingType bindingKeyType = null;
        TemplateBindingType bindingElementType = null;
        var passBy = "<None>";

        if (type instanceof DSMTypeSet typeSet) {
            ctype = TemplateFieldType.SET;
            elementType = convertType(typeSet.elementType);
            elementTypeSuffix = typeSuffix(typeSet.elementType);
            elementTypeViperValue = viperValue(typeSet.elementType);
            bindingElementType = templateBindingType(typeSet.elementType);
            passBy = passByQualifier(typeSet.elementType);
        }

        if (type instanceof DSMTypeMap typeMap) {
            ctype = TemplateFieldType.MAP;
            keyType = convertType(typeMap.keyType);
            keyTypeSuffix = typeSuffix(typeMap.keyType);
            elementType = convertType(typeMap.elementType);
            elementTypeSuffix = typeSuffix(typeMap.elementType);
            elementTypeViperValue = viperValue(typeMap.elementType);
            bindingKeyType = templateBindingType(typeMap.keyType);
            bindingElementType = templateBindingType(typeMap.elementType);
            passBy = passByQualifier(typeMap.elementType);
        }

        if (type instanceof DSMTypeXArray typeXArray) {
            ctype = TemplateFieldType.XARRAY;
            elementType = convertType(typeXArray.elementType);
            elementTypeSuffix = typeSuffix(typeXArray.elementType);
            elementTypeViperValue = viperValue(typeXArray.elementType);
            bindingElementType = templateBindingType(typeXArray.elementType);
            passBy = passByQualifier(typeXArray.elementType);
        }

        return new TemplateField(ctype, keyType, keyTypeSuffix, elementType, elementTypeSuffix, elementTypeViperValue, bindingKeyType, bindingElementType, passBy);
    }

    TemplateBindingType templateBindingType(DSMType type) throws Exception {
        return templateBindingType(null, type);
    }

    TemplateBindingType templateBindingType(NameSpace nameSpace, DSMType type) throws Exception {
        final var proxy = bindingType(type);
        final var typeSuffix = typeSuffix(type);
        final var useProxy = needsProxy(proxy);
        final var inNamespace = bindingTypeInNamespace(nameSpace, type);
        final var isNamed = isNamedType(type);
        final var annotation = bindingAnnotationInNamespace(nameSpace, type);

        if (useProxy || vocabulary == null)
            return new TemplateBindingType(proxy, typeSuffix,
                                           useProxy ? proxy : null,
                                           useProxy ? inNamespace : null, useProxy, isNamed, annotation);

        return new TemplateBindingType(proxy, typeSuffix,
                                       vocabulary.leaf(proxy), vocabulary.leaf(proxy), false, isNamed, annotation);
    }

    /** Whether a unit declares this type, rather than it being built from others. */
    private boolean isNamedType(DSMType type) {
        if (type instanceof DSMTypeKey typeKey)
            return isNamedType(typeKey.elementType);

        return type instanceof DSMTypeReference reference
            && switch (reference.domain) {
                   case ENUMERATION, STRUCTURE, CONCEPT, CLUB -> true;
                   default -> false;
               };
    }

    /**
     * The proxy name, spelled as the unit {@code nameSpace} can write it.
     *
     * <p>The flat spelling — {@code ModelA_Colour} — exists because a package whose modules
     * are one flat file has nowhere else to put the unit's name. When a unit is a module,
     * it has somewhere: {@code Colour} inside ModelA, {@code modela.Colour} anywhere else.
     * The pair is the same one {@link #convertType} and {@link #convertTypeInNamespace} form
     * for the native target, and it has to stay in step for the same reason — in-unit and
     * cross-unit renderings that drift produce code that compiles in one file and not in
     * the next.
     *
     * <p>Only a reference carries a unit; a container is written from its elements, so it
     * qualifies wherever they do.
     *
     * <p>A null {@code nameSpace} is not a missing answer but a real one: a pool belongs to
     * no namespace, so every named type it mentions is foreign to it and every one of them
     * qualifies. The same holds for what the model carries rather than a unit.
     */
    String bindingTypeInNamespace(NameSpace nameSpace, DSMType type) throws Exception {
        if (type instanceof DSMTypeKey typeKey)
            return bindingTypeInNamespace(nameSpace, typeKey.elementType);

        if (type instanceof DSMTypeReference typeReference)
            return switch (typeReference.domain) {
                case ENUMERATION, STRUCTURE, CONCEPT, CLUB -> {
                    final var unit = typeReference.typeName.nameSpace;
                    final var bare = typeReference.typeName.name
                                   + (typeReference.domain == DSMTypeReferenceDomain.CONCEPT
                                      || typeReference.domain == DSMTypeReferenceDomain.CLUB ? "Key" : "");
                    yield nameSpace != null && unit.equals(nameSpace)
                        ? bare
                        : TemplateTool.lsc(unit.name) + "." + bare;
                }
                default -> bindingType(type);
            };

        return bindingType(type);
    }

    /**
     * The type as a target writes it in an annotation, from inside the unit {@code nameSpace}.
     *
     * <p>THE ONE PLACE THE STRENGTH OF THE GENERATED TYPES IS DECIDED. A binding that emits a
     * class per container shape gets its annotation for free — the class is the annotation.
     * One that does not has to spell the shape here, and anything less than the whole shape
     * is a checker made blind: {@code Any} accepts every assignment, so a field that holds
     * colours would take an integer without a word.
     *
     * <p>Recursive, and the spellings are the binding's rather than this method's: what a
     * sequence or an association is called is a property of the target, stated once in its
     * vocabulary.
     */
    String bindingAnnotationInNamespace(NameSpace nameSpace, DSMType type) throws Exception {
        if (vocabulary == null)
            return null;

        if (type instanceof DSMTypeKey typeKey)
            return bindingAnnotationInNamespace(nameSpace, typeKey.elementType);

        if (type instanceof DSMTypeVec typeVec)
            return vocabulary.list(bindingAnnotationInNamespace(nameSpace, typeVec.elementType));

        if (type instanceof DSMTypeMat typeMat)
            return vocabulary.matrix(bindingAnnotationInNamespace(nameSpace, typeMat.elementType));

        if (type instanceof DSMTypeTuple typeTuple)
            return vocabulary.tuple(annotations(nameSpace, typeTuple.types));

        if (type instanceof DSMTypeOptional typeOptional)
            return vocabulary.optional(bindingAnnotationInNamespace(nameSpace, typeOptional.elementType));

        if (type instanceof DSMTypeVector typeVector)
            return vocabulary.list(bindingAnnotationInNamespace(nameSpace, typeVector.elementType));

        if (type instanceof DSMTypeSet typeSet)
            return vocabulary.list(bindingAnnotationInNamespace(nameSpace, typeSet.elementType));

        if (type instanceof DSMTypeMap typeMap)
            return vocabulary.map(bindingAnnotationInNamespace(nameSpace, typeMap.keyType),
                                  bindingAnnotationInNamespace(nameSpace, typeMap.elementType));

        if (type instanceof DSMTypeXArray typeXArray)
            return vocabulary.ordered(bindingAnnotationInNamespace(nameSpace, typeXArray.elementType));

        if (type instanceof DSMTypeVariant typeVariant)
            return vocabulary.union(annotations(nameSpace, typeVariant.types));

        if (type instanceof DSMTypeReference reference)
            switch (reference.domain) {
                case ENUMERATION, STRUCTURE, CONCEPT, CLUB -> {
                    return bindingTypeInNamespace(nameSpace, type);
                }
                case ANY_CONCEPT -> {
                    return "AnyConceptKey";
                }
                case ANY -> {
                    return vocabulary.any();
                }
                case PRIMITIVE -> {
                    return vocabulary.leaf(reference.typeName.name);
                }
            }

        throw new ConvertException(
            String.format("bindingAnnotationInNamespace: type '%s' is not handled.", type.representation()));
    }

    private ArrayList<String> annotations(NameSpace nameSpace, ArrayList<DSMType> types) throws Exception {
        final var result = new ArrayList<String>();
        for (var type : types)
            result.add(bindingAnnotationInNamespace(nameSpace, type));

        return result;
    }

    private String bindingType(DSMType type) throws Exception {
        if (type instanceof DSMTypeKey typeKey) {
            return bindingType(typeKey.elementType);
        }

        if (type instanceof DSMTypeVec typeVec)
            return String.format("Vec_%s_%d", bindingType(typeVec.elementType), typeVec.size);

        if (type instanceof DSMTypeMat typeMat)
            return String.format("Mat_%s_%d_%d", bindingType(typeMat.elementType), typeMat.columns, typeMat.rows);

        if (type instanceof DSMTypeTuple typeTuple) {
            var memberSuffixes = new ArrayList<String>();
            for (var memberType : typeTuple.types)
                memberSuffixes.add(bindingType(memberType));

            return "Tuple_" + String.join("_", memberSuffixes);
        }

        if (type instanceof DSMTypeOptional typeOptional)
            return String.format("Optional_%s", bindingType(typeOptional.elementType));

        if (type instanceof DSMTypeVector typeVector)
            return String.format("Vector_%s", bindingType(typeVector.elementType));

        if (type instanceof DSMTypeMap typeMap)
            return String.format("Map_%s_to_%s", bindingType(typeMap.keyType), bindingType(typeMap.elementType));

        if (type instanceof DSMTypeSet typeSet)
            return String.format("Set_%s", bindingType(typeSet.elementType));

        if (type instanceof DSMTypeXArray typeXArray)
            return String.format("XArray_%s", bindingType(typeXArray.elementType));

        if (type instanceof DSMTypeVariant typeVariant) {
            var memberSuffixes = new ArrayList<String>();
            for (var memberType : typeVariant.types)
                memberSuffixes.add(bindingType(memberType));

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

        throw new ConvertException(String.format("bindingType: Type '%s' not handled.", type.representation()));
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

    private boolean isTypeTupleMovable(DSMTypeTuple typeTuple) {
        for (var type : typeTuple.types)
            if (isTypeMovable(type))
                return true;

        return false;
    }

    private boolean isTypeVariantMovable(DSMTypeVariant typeVariant) {
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
