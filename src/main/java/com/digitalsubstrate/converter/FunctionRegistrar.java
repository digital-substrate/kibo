package com.digitalsubstrate.converter;

import com.digitalsubstrate.viper.dsm.*;

import java.util.HashMap;

final class FunctionRegistrar {
    final HashMap<String, DSMTypeVec> vecFunctions = new HashMap<>();
    final HashMap<String, DSMTypeMat> matFunctions = new HashMap<>();
    final HashMap<String, DSMTypeTuple> tupleFunctions = new HashMap<>();
    final HashMap<String, DSMTypeOptional> optionalFunctions = new HashMap<>();
    final HashMap<String, DSMTypeVector> vectorFunctions = new HashMap<>();
    final HashMap<String, DSMTypeSet> setFunctions = new HashMap<>();
    final HashMap<String, DSMTypeMap> mapFunctions = new HashMap<>();
    final HashMap<String, DSMTypeXArray> xarrayFunctions = new HashMap<>();
    final HashMap<String, DSMTypeVariant> variantFunctions = new HashMap<>();

    private final DSMDefinitions definitions;
    private final TypeConverter typeConverter;

    FunctionRegistrar(DSMDefinitions definitions, TypeConverter typeConverter) {
        this.definitions = definitions;
        this.typeConverter = typeConverter;
    }

    void registerFunctionForStructures() throws Exception {
        for (var structure : definitions.structures)
            registerFunctionForStructure(structure);
    }

    private void registerFunctionForStructure(DSMStructure structure) throws Exception {
        for (var field : structure.fields)
            registerFunctionForContainer(field.type);
    }

    void registerFunctionForContainer(DSMType type) throws Exception {

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
}
