package com.digitalsubstrate.converter;

import com.digitalsubstrate.viper.NameSpace;
import com.digitalsubstrate.viper.TypeName;
import com.digitalsubstrate.viper.dsm.*;

import java.util.ArrayList;
import java.util.HashMap;

final class TypeConverter {
    private final HashMap<String, String> cppPrimitiveTypes;
    private final HashMap<String, String> viperPrimitiveValues;

    TypeConverter(HashMap<String, String> cppPrimitiveTypes, HashMap<String, String> viperPrimitiveValues) {
        this.cppPrimitiveTypes = cppPrimitiveTypes;
        this.viperPrimitiveValues = viperPrimitiveValues;
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
}
