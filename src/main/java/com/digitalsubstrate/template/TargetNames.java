package com.digitalsubstrate.template;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * What the project and the template pack say about names, for the target of this run.
 *
 * <p>A DSM name is valid in every language or in none of them, and the model is never changed for
 * one target. When a target cannot spell a name -- a field {@code class} in C++ -- or a name meets
 * one of the pack's own -- a field {@code wrap_value} beside the proxy's method -- the project
 * writes how the name is spelled in that target ({@code --spell}, {@code [names.<target>.rename]}
 * in kibo-project). Nothing is renamed otherwise: kibo identifies a clash with the pack's names
 * and says which directive to write, the project corrects.
 *
 * <p>{@code spell} is the name every identifier of the target uses; the DSM name stays the one the
 * runtime knows ({@code dsmName} in the Template Model).
 */
public final class TargetNames {

    /** The families of names a pack declares its own names for. */
    public static final Set<String> KINDS = Set.of(
        "namespace", "pool", "type", "field", "case", "attachment", "function", "parameter");

    private final Map<String, String> spellings;
    private final Map<String, Set<String>> reserved;

    private TargetNames(Map<String, String> spellings, Map<String, Set<String>> reserved) {
        this.spellings = Map.copyOf(spellings);
        final var copy = new LinkedHashMap<String, Set<String>>();
        reserved.forEach((kind, names) -> copy.put(kind, Set.copyOf(names)));
        this.reserved = Map.copyOf(copy);
    }

    public static TargetNames none() {
        return new TargetNames(Map.of(), Map.of());
    }

    public static TargetNames of(Map<String, String> spellings, Map<String, Set<String>> reserved) {
        return new TargetNames(spellings, reserved);
    }

    /** How the target spells a DSM name: as the project says, or as declared. */
    public String spell(String dsmName) {
        return spellings.getOrDefault(dsmName, dsmName);
    }

    /** The names the pack's own code takes in this target, for one family of names. */
    public Set<String> reserved(String kind) {
        return reserved.getOrDefault(kind, Set.of());
    }
}
