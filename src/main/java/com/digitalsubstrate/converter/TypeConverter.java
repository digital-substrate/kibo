package com.digitalsubstrate.converter;

import com.digitalsubstrate.template.TemplateField;
import com.digitalsubstrate.template.TemplateFieldType;
import com.digitalsubstrate.template.TemplatePythonType;
import com.digitalsubstrate.viper.NameSpace;
import com.digitalsubstrate.viper.TypeName;
import com.digitalsubstrate.viper.dsm.*;

import java.util.ArrayList;
import java.util.HashMap;

final class TypeConverter {
    private final HashMap<String, String> cppPrimitiveTypes;
    private final HashMap<String, String> viperPrimitiveValues;
    private final HashMap<TypeName, DSMStructure> structuresByTypeName;

    TypeConverter(HashMap<String, String> cppPrimitiveTypes,
                  HashMap<String, String> viperPrimitiveValues,
                  HashMap<TypeName, DSMStructure> structuresByTypeName) {
        this.cppPrimitiveTypes = cppPrimitiveTypes;
        this.viperPrimitiveValues = viperPrimitiveValues;
        this.structuresByTypeName = structuresByTypeName;
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
                    return "AnyConceptKey";
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

    TemplatePythonType templatePythonType(DSMType type) throws Exception {
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
