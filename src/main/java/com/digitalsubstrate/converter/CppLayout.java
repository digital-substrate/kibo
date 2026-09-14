package com.digitalsubstrate.converter;

/**
 * One flat output directory, and the unit's name carried by every file.
 *
 * <p>This is the convention the ecosystem already follows, for generated and
 * hand-written code alike: {@code Viper_FunctionPool.hpp}, {@code GE_Data.hpp},
 * {@code RaptorMath_Aabb2.hpp}. Includes carry no path and the build supplies one
 * {@code -I} per unit, so a unit's directory is a build concern and never a code one —
 * which is what lets a unit move, or be published on its own, without an emitted byte
 * changing.
 *
 * <p>The prefix is therefore load-bearing rather than decorative: with path-less
 * includes the file namespace is flat across the whole build, and the unit's name is
 * the only thing keeping two units' {@code Data} headers apart.
 */
public final class CppLayout implements TargetLayout {

    @Override
    public String outputFileName(String unit, String templateBaseName) {
        return unit + "_" + templateBaseName;
    }

    @Override
    public String artefactPath(String unit, String artefact) {
        return unit + "_" + artefact + ".hpp";
    }
}
