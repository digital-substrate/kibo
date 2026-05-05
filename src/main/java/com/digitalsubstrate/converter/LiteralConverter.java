package com.digitalsubstrate.converter;

import com.digitalsubstrate.template.TemplateTool;
import com.digitalsubstrate.viper.TypeName;
import com.digitalsubstrate.viper.dsm.*;

import java.util.ArrayList;
import java.util.HashMap;

final class LiteralConverter {
    private final HashMap<TypeName, DSMStructure> structuresByTypeName;

    LiteralConverter(HashMap<TypeName, DSMStructure> structuresByTypeName) {
        this.structuresByTypeName = structuresByTypeName;
    }

    String convertRootDefaultValue(DSMLiteral literal, DSMType type) {
        if (literal instanceof DSMLiteralValue literalValue) {
            if (literalValue.domain == DSMLiteralDomain.NONE)
                return "{}";

            return "{" + convertDefaultValue(literal, type) + "}";
        }

        return convertDefaultValue(literal, type);
    }

    String convertDefaultValue(DSMLiteral literal, DSMType type) {
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
}
