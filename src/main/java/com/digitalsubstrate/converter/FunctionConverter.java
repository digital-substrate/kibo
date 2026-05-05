package com.digitalsubstrate.converter;

import com.digitalsubstrate.template.*;
import com.digitalsubstrate.viper.dsm.*;

import java.util.ArrayList;

final class FunctionConverter {
    private final DSMDefinitions definitions;
    private final TypeConverter typeConverter;
    private final FunctionRegistrar functionRegistrar;

    FunctionConverter(DSMDefinitions definitions, TypeConverter typeConverter, FunctionRegistrar functionRegistrar) {
        this.definitions = definitions;
        this.typeConverter = typeConverter;
        this.functionRegistrar = functionRegistrar;
    }

    void emitContainerFunctions(TemplateDefinitions definitions) throws Exception {

        for (var vec : functionRegistrar.vecFunctions.values()) {
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

        for (var mat : functionRegistrar.matFunctions.values()) {
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

        for (var tuple : functionRegistrar.tupleFunctions.values()) {
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

        for (var optional : functionRegistrar.optionalFunctions.values()) {
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

        for (var vector : functionRegistrar.vectorFunctions.values()) {
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

        for (var set : functionRegistrar.setFunctions.values()) {
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

        for (var map : functionRegistrar.mapFunctions.values()) {
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

        for (var xarray : functionRegistrar.xarrayFunctions.values()) {
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

        for (var variant : functionRegistrar.variantFunctions.values()) {
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

    ArrayList<TemplateFunctionPool> convertFunctionPool() throws Exception {
        final var result = new ArrayList<TemplateFunctionPool>();
        for (var pool : definitions.functionPools)
            result.add(convertFunctionPool(pool));

        return result;
    }

    private TemplateFunctionPool convertFunctionPool(DSMFunctionPool pool) throws Exception {
        final var functions = new ArrayList<TemplateFunction>();
        for (var function : pool.functions)
            functions.add(convertFunction(function));

        return new TemplateFunctionPool(functions, pool);
    }

    private TemplateFunction convertFunction(DSMFunction function) throws Exception {
        final var parameters = convertFunctionParameters(function.prototype.parameters);
        final var type = typeConverter.convertType(function.prototype.returnType);
        final var typeSuffix = typeConverter.typeSuffix(function.prototype.returnType);
        final var returnViperValue = typeConverter.viperValue(function.prototype.returnType);
        final var returnPythonType = typeConverter.templatePythonType(function.prototype.returnType);
        functionRegistrar.registerFunctionForContainer(function.prototype.returnType);

        return new TemplateFunction(
                function, type, typeSuffix, parameters,
                returnViperValue, returnPythonType);
    }

    ArrayList<TemplateAttachmentFunctionPool> convertAttachmentFunctionPool() throws Exception {
        final var result = new ArrayList<TemplateAttachmentFunctionPool>();
        for (var pool : definitions.attachmentFunctionPools)
            result.add(convertAttachmentFunctionPool(pool));

        return result;
    }

    private TemplateAttachmentFunctionPool convertAttachmentFunctionPool(DSMAttachmentFunctionPool pool) throws Exception {
        final var functions = new ArrayList<TemplateAttachmentFunction>();
        for (var function : pool.functions)
            functions.add(convertAttachmentFunction(function));

        return new TemplateAttachmentFunctionPool(functions, pool);
    }

    private TemplateAttachmentFunction convertAttachmentFunction(DSMAttachmentFunction function) throws Exception {
        final var parameters = convertFunctionParameters(function.prototype.parameters);
        final var type = typeConverter.convertType(function.prototype.returnType);
        final var typeSuffix = typeConverter.typeSuffix(function.prototype.returnType);
        final var returnViperValue = typeConverter.viperValue(function.prototype.returnType);
        final var templatePythonType = typeConverter.templatePythonType(function.prototype.returnType);
        functionRegistrar.registerFunctionForContainer(function.prototype.returnType);

        return new TemplateAttachmentFunction(
                function, type, typeSuffix, parameters,
                returnViperValue, templatePythonType);
    }

    private ArrayList<TemplateFunctionParameter> convertFunctionParameters(ArrayList<DSMFunctionPrototypeParameter> parameters) throws Exception {
        final var result = new ArrayList<TemplateFunctionParameter>();
        for (var parameter : parameters) {
            functionRegistrar.registerFunctionForContainer(parameter.type);
            result.add(convertFunctionParameter(parameter));
        }

        return result;
    }

    private TemplateFunctionParameter convertFunctionParameter(DSMFunctionPrototypeParameter parameter) throws Exception {
        final var type = typeConverter.convertType(parameter.type);
        final var passBy = typeConverter.passByQualifier(parameter.type);
        final var typeSuffix = typeConverter.typeSuffix(parameter.type);
        final var viperValue = typeConverter.viperValue(parameter.type);
        final var templatePythonType = typeConverter.templatePythonType(parameter.type);

        return new TemplateFunctionParameter(parameter, type, passBy, typeSuffix, viperValue, templatePythonType);
    }
}
