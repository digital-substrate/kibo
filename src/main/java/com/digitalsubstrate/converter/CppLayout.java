package com.digitalsubstrate.converter;

import com.digitalsubstrate.template.TemplateTool;

/**
 * One flat output directory, every file named by the namespace path of what it declares.
 *
 * <p>{@code <model>_<unit>_<artefact>}, in lower snake case -- {@code gei_graph_data.hpp} for
 * the {@code Data} of the unit {@code Graph} in the model {@code gei}, {@code gei_codec.hpp}
 * for the model's own {@code Codec}. The C++ namespace path, spelled as a file: generated names
 * are lower case, as generated namespaces are, and an application's own capitalised ones
 * ({@code GE_Graph_Integrity.hpp}) never meet them.
 *
 * <p>Includes carry no path and the build supplies the directory, so the name is what keeps
 * two headers apart across the whole build. Prefixed by the model, two projects that both
 * declare a namespace {@code Core} no longer both write {@code Core_Data.hpp} -- nor both
 * guard it with the same macro.
 */
public final class CppLayout implements TargetLayout {

    @Override
    public String outputFileName(Scope scope, String model, String unit, String templateBaseName) {
        final var dot = templateBaseName.indexOf('.');
        final var stem = dot < 0 ? templateBaseName : templateBaseName.substring(0, dot);
        final var extension = dot < 0 ? "" : templateBaseName.substring(dot);
        return prefix(scope, model, unit) + "_" + TemplateTool.lsc(stem) + extension;
    }

    @Override
    public String artefactPath(Scope scope, String model, String unit, String artefact) {
        return prefix(scope, model, unit) + "_" + TemplateTool.lsc(artefact) + ".hpp";
    }

    private static String prefix(Scope scope, String model, String unit) {
        return scope == Scope.MODEL ? model : model + "_" + TemplateTool.lsc(unit);
    }
}
