package com.digitalsubstrate.converter;

import com.digitalsubstrate.template.*;
import com.digitalsubstrate.viper.dsm.*;

import java.util.ArrayList;
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

    void emitContainerFunctions(TemplateDefinitions definitions) throws Exception {

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
}
