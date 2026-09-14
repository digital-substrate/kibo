package com.digitalsubstrate.template;

import com.digitalsubstrate.converter.TargetLayout;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Set;

/**
 * Where a template finds another generated artefact.
 *
 * <p>A template declares what it needs by name — {@code <m.include.Data>} — and this
 * answers with the path. It does not compose one: the artefact name is the template's,
 * the location is the generator's, and neither knows the other's half.
 *
 * <p>That split is what lets an artefact move. A template that composed
 * {@code "<m.namespace>_Data.hpp"} would have to be edited every time the layout
 * changed, and every template that included a moved artefact would break at once. Here
 * the call site is unchanged and re-resolves.
 *
 * <p>Every key answers, because the set of artefacts is the template pack's business
 * and kibo never sees a pack — only whatever {@code -t} points at. A name that matches
 * nothing on disk is a missing include at compile time, which is where it belongs.
 */
public final class TemplateIncludePaths extends AbstractMap<String, String> {

    private final TargetLayout layout;
    private final String unit;

    public TemplateIncludePaths(TargetLayout layout, String unit) {
        this.layout = layout;
        this.unit = unit;
    }

    @Override
    public String get(Object artefact) {
        return layout.artefactPath(unit, String.valueOf(artefact));
    }

    @Override
    public boolean containsKey(Object artefact) {
        return true;
    }

    @Override
    public Set<Map.Entry<String, String>> entrySet() {
        return Set.of();
    }
}
