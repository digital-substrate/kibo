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
    private final Binding binding;

    FunctionRegistrar(DSMDefinitions definitions, TypeConverter typeConverter, Binding binding) {
        this.definitions = definitions;
        this.typeConverter = typeConverter;
        this.binding = binding;
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
            if (binding.needsDerivedContainerProxies())
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
            final var bindingType = typeConverter.templateBindingType(vec);
            final var bindingElementType = typeConverter.templateBindingType(vec.elementType);

            definitions.vecFunctions.add(new TemplateVecFunction(
                    type, vec.size, typeSuffix, elementTypeSuffix,
                    dsmType,
                    bindingType, bindingElementType,
                    typeConverter.bindingSequence(bindingElementType.getType(), vec.size)));
        }

        for (var mat : matFunctions.values()) {
            final var type = typeConverter.convertType(mat);
            final var typeSuffix = typeConverter.typeSuffix(mat);
            final var elementTypeSuffix = typeConverter.typeSuffix(mat.elementType);
            final var dsmType = mat.representation();
            final var bindingType = typeConverter.templateBindingType(mat);
            final var bindingElementType = typeConverter.templateBindingType(mat.elementType);

            final var bindingColumnType = typeConverter.bindingSequence(bindingElementType.getType(), mat.rows);

            definitions.matFunctions.add(new TemplateMatFunction(
                    type, mat.columns, mat.rows, typeSuffix, elementTypeSuffix,
                    dsmType,
                    bindingType, bindingElementType,
                    typeConverter.bindingSequence(bindingColumnType, mat.columns), bindingColumnType));
        }

        for (var tuple : tupleFunctions.values()) {
            final var type = typeConverter.convertType(tuple);
            final var typeSuffix = typeConverter.typeSuffix(tuple);
            final var members = new ArrayList<TemplateType>();
            final var dsmType = tuple.representation();
            final var bindingType = typeConverter.templateBindingType(tuple);

            for (var memberType : tuple.types)
                members.add(new TemplateType(memberType.representation(),
                                             typeConverter.convertType(memberType),
                                             typeConverter.typeSuffix(memberType),
                                             typeConverter.templateBindingType(memberType)));

            definitions.tupleFunctions.add(new TemplateTupleFunction(
                    type, typeSuffix, members,
                    dsmType,
                    bindingType));
        }

        for (var optional : optionalFunctions.values()) {
            final var type = typeConverter.convertType(optional);
            final var typeSuffix = typeConverter.typeSuffix(optional);
            final var elementType = typeConverter.convertType(optional.elementType);
            final var elementTypeSuffix = typeConverter.typeSuffix(optional.elementType);
            final var dsmType = optional.representation();
            final var bindingType = typeConverter.templateBindingType(optional);
            final var bindingElementType = typeConverter.templateBindingType(optional.elementType);

            definitions.optionalFunctions.add(new TemplateOptionalFunction(
                    type, typeSuffix, elementType, elementTypeSuffix,
                    dsmType,
                    bindingType, bindingElementType));
        }

        for (var vector : vectorFunctions.values()) {
            final var type = typeConverter.convertType(vector);
            final var typeSuffix = typeConverter.typeSuffix(vector);
            final var elementTypeSuffix = typeConverter.typeSuffix(vector.elementType);
            final var dsmType = vector.representation();
            final var bindingType = typeConverter.templateBindingType(vector);
            final var bindingElementType = typeConverter.templateBindingType(vector.elementType);

            definitions.vectorFunctions.add(new TemplateVectorFunction(
                    type, typeSuffix, elementTypeSuffix,
                    dsmType,
                    bindingType, bindingElementType));
        }

        for (var set : setFunctions.values()) {
            final var type = typeConverter.convertType(set);
            final var typeSuffix = typeConverter.typeSuffix(set);
            final var elementTypeSuffix = typeConverter.typeSuffix(set.elementType);
            final var dsmType = set.representation();
            final var bindingType = typeConverter.templateBindingType(set);
            final var bindingElementType = typeConverter.templateBindingType(set.elementType);

            definitions.setFunctions.add(new TemplateSetFunction(
                    type, typeSuffix, elementTypeSuffix,
                    dsmType,
                    bindingType, bindingElementType));
        }

        for (var map : mapFunctions.values()) {
            final var type = typeConverter.convertType(map);
            final var typeSuffix = typeConverter.typeSuffix(map);
            final var keyTypeSuffix = typeConverter.typeSuffix(map.keyType);
            final var elementTypeSuffix = typeConverter.typeSuffix(map.elementType);
            final var dsmType = map.representation();
            final var bindingType = typeConverter.templateBindingType(map);
            final var bindingKeyType = typeConverter.templateBindingType(map.keyType);
            final var bindingElementType = typeConverter.templateBindingType(map.elementType);

            definitions.mapFunctions.add(new TemplateMapFunction(
                    type, typeSuffix, keyTypeSuffix, elementTypeSuffix,
                    dsmType,
                    bindingType, bindingKeyType, bindingElementType));
        }

        for (var xarray : xarrayFunctions.values()) {
            final var type = typeConverter.convertType(xarray);
            final var typeSuffix = typeConverter.typeSuffix(xarray);
            final var elementType = typeConverter.convertType(xarray.elementType);
            final var elementTypeSuffix = typeConverter.typeSuffix(xarray.elementType);
            final var dsmType = xarray.representation();
            final var bindingType = typeConverter.templateBindingType(xarray);
            final var bindingElementType = typeConverter.templateBindingType(xarray.elementType);

            definitions.xarrayFunctions.add(new TemplateXArrayFunction(type, typeSuffix, elementType, elementTypeSuffix,
                    dsmType,
                    bindingType, bindingElementType));
        }

        for (var variant : variantFunctions.values()) {
            final var type = typeConverter.convertType(variant);
            final var typeSuffix = typeConverter.typeSuffix(variant);
            final var members = new ArrayList<TemplateType>();
            final var dsmType = variant.representation();
            final var bindingType = typeConverter.templateBindingType(variant);

            for (var memberType : variant.types)
                members.add(new TemplateType(memberType.representation(),
                                             typeConverter.convertType(memberType),
                                             typeConverter.typeSuffix(memberType),
                                             typeConverter.templateBindingType(memberType)));

            definitions.variantFunctions.add(new TemplateVariantFunction(
                    type, typeSuffix, members,
                    dsmType,
                    bindingType));
        }
    }
}
