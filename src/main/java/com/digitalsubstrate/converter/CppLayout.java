package com.digitalsubstrate.converter;

import com.digitalsubstrate.template.TemplateTool;

/**
 * One flat output directory, every file named by the namespace path of what it declares.
 *
 * <p>{@code <model>_<unit>_<artefact>} -- {@code gei_graph_data.hpp} for the {@code data} of the
 * unit {@code Graph} in the model {@code gei}, {@code gei_codec.hpp} for the model's own
 * {@code codec}. The C++ namespace path, spelled as a file: the unit, which comes from the model,
 * is written in lower snake case, as generated namespaces are.
 *
 * <p>The artefact is the template's own name, taken as given, as {@code -n} is: a pack that
 * names its templates in lower case ({@code data.hpp.stg}) gets lower-case files, and a
 * project's own template keeps its project's casing -- {@code State.hpp.stg} rendered under
 * {@code -n RaptorLogic} is {@code RaptorLogic_State.hpp}, beside {@code RaptorLogic_Camera.hpp}
 * written by hand. The name belongs to whoever writes the template, not to kibo.
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
        return prefix(scope, model, unit) + "_" + stem + extension;
    }

    @Override
    public String artefactPath(Scope scope, String model, String unit, String artefact) {
        return prefix(scope, model, unit) + "_" + artefact + ".hpp";
    }

    private static String prefix(Scope scope, String model, String unit) {
        return scope == Scope.MODEL ? model : model + "_" + TemplateTool.lsc(unit);
    }
}
