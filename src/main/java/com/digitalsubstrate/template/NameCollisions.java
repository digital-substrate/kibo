package com.digitalsubstrate.template;

import com.digitalsubstrate.viper.dsm.DSMAttachment;
import com.digitalsubstrate.viper.dsm.DSMAttachmentFunction;
import com.digitalsubstrate.viper.dsm.DSMAttachmentFunctionPool;
import com.digitalsubstrate.viper.dsm.DSMDefinitions;
import com.digitalsubstrate.viper.dsm.DSMEnumeration;
import com.digitalsubstrate.viper.dsm.DSMEnumerationCase;
import com.digitalsubstrate.viper.dsm.DSMFunction;
import com.digitalsubstrate.viper.dsm.DSMFunctionPool;
import com.digitalsubstrate.viper.dsm.DSMFunctionPrototype;
import com.digitalsubstrate.viper.dsm.DSMFunctionPrototypeParameter;
import com.digitalsubstrate.viper.dsm.DSMStructure;
import com.digitalsubstrate.viper.dsm.DSMStructureField;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * Two DSM names that a package spells alike, scope by scope.
 *
 * <p>The snake_case of a static name is a projection, and two names can meet: {@code f_E} and
 * {@code f_e} in one structure, a namespace {@code Containers} and the package's
 * {@code containers} module. Rendering would then write one name over the other, and the
 * generated code would be wrong without a word. Each scope is checked before anything is
 * rendered, and a collision names both DSM names and the way out: a rename in the project.
 */
public final class NameCollisions {

    /** The modules every package declares at its root, beside the namespaces and pools. */
    static final List<String> ROOT_MODULES = List.of("containers", "definitions", "resources", "pools");

    private NameCollisions() {}

    public static List<String> find(DSMDefinitions definitions) {
        final var found = new ArrayList<String>();
        final Function<String, String> snake = TemplateTool::snake;
        final Function<String, String> upper = TemplateTool::usnake;

        final var root = new ArrayList<String[]>();
        final var namespaces = new LinkedHashSet<String>();
        for (var s : definitions.structures) namespaces.add(s.typeName.nameSpace.name);
        for (var e : definitions.enumerations) namespaces.add(e.typeName.nameSpace.name);
        for (var c : definitions.concepts) namespaces.add(c.typeName.nameSpace.name);
        for (var c : definitions.clubs) namespaces.add(c.typeName.nameSpace.name);
        for (var a : definitions.attachments) namespaces.add(a.typeName.nameSpace.name);
        for (var n : namespaces) root.add(new String[]{"namespace " + n, n});
        for (var p : definitions.functionPools) root.add(new String[]{"pool " + p.name, p.name});
        for (var p : definitions.attachmentFunctionPools) root.add(new String[]{"pool " + p.name, p.name});
        for (var m : ROOT_MODULES) root.add(0, new String[]{"the package's own module " + m, m});
        checkLabelled(found, "the package root", root, snake);

        for (DSMStructure s : definitions.structures)
            check(found, "structure " + s.typeName.representation(),
                  s.fields.stream().map((DSMStructureField f) -> f.name).toList(), snake, List.of());

        for (DSMEnumeration e : definitions.enumerations)
            check(found, "enumeration " + e.typeName.representation(),
                  e.members.stream().map((DSMEnumerationCase c) -> c.name).toList(), upper, List.of());

        final Map<String, List<String>> byKey = new LinkedHashMap<>();
        for (DSMAttachment a : definitions.attachments)
            byKey.computeIfAbsent(a.keyType.representation(), k -> new ArrayList<>()).add(a.typeName.name);
        byKey.forEach((key, names) -> check(found, "the attachments of " + key, names, snake, List.of()));

        for (DSMFunctionPool p : definitions.functionPools) {
            check(found, "pool " + p.name, p.functions.stream().map((DSMFunction f) -> f.prototype.name).toList(),
                  snake, List.of());
            for (DSMFunction f : p.functions)
                parameters(found, p.name, f.prototype, snake);
        }
        for (DSMAttachmentFunctionPool p : definitions.attachmentFunctionPools) {
            check(found, "pool " + p.name,
                  p.functions.stream().map((DSMAttachmentFunction f) -> f.prototype.name).toList(), snake, List.of());
            for (DSMAttachmentFunction f : p.functions)
                parameters(found, p.name, f.prototype, snake);
        }
        return found;
    }

    private static void parameters(List<String> found, String pool, DSMFunctionPrototype prototype,
                                   Function<String, String> snake) {
        check(found, "the parameters of " + pool + "." + prototype.name,
              prototype.parameters.stream().map((DSMFunctionPrototypeParameter p) -> p.name).toList(), snake,
              List.of());
    }

    private static void check(List<String> found, String scope, List<String> names,
                              Function<String, String> projection, List<String> unused) {
        checkLabelled(found, scope, names.stream().map(n -> new String[]{n, n}).toList(), projection);
    }

    // Each entry is {label, DSM name}: the label tells two things of one name apart (a namespace
    // and a pool both called Tools).
    private static void checkLabelled(List<String> found, String scope, List<String[]> entries,
                                      Function<String, String> projection) {
        final Map<String, String> seen = new LinkedHashMap<>();
        final Set<String> reported = new LinkedHashSet<>();
        for (var entry : entries) {
            final var projected = entry[0].startsWith("the package's own module ") ? entry[1] : projection.apply(entry[1]);
            final var other = seen.putIfAbsent(projected, entry[0]);
            if (other != null && !other.equals(entry[0]) && reported.add(projected))
                found.add(scope + ": " + other + " and " + entry[0] + " are both spelled " + projected
                          + " -- spell one as you want in the project: [names.rename] in kibo.toml, or kibo's --rename "
                          + entry[1] + "=...");
        }
    }
}
