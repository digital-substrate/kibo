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
import java.util.LinkedHashSet;
import java.util.List;

/**
 * DSM names that meet a name the template pack's own code takes, in the target of this run.
 *
 * <p>A pack declares, per family of names, what its code already calls something
 * ({@code --reserve field:wrap_value}): a field {@code wrap_value} would mask the method every
 * generated class has, a namespace {@code Containers} would take the place of the package's
 * {@code containers} module. kibo does not rename either: it says which name meets which, and the
 * directive that spells it otherwise for this target. The project corrects.
 */
public final class ReservedNames {

    private ReservedNames() {}

    public static List<String> find(DSMDefinitions definitions, String target) {
        final var names = TemplateTool.targetNames();
        final var found = new ArrayList<String>();

        final var namespaces = new LinkedHashSet<String>();
        for (var s : definitions.structures) namespaces.add(s.typeName.nameSpace.name);
        for (var e : definitions.enumerations) namespaces.add(e.typeName.nameSpace.name);
        for (var c : definitions.concepts) namespaces.add(c.typeName.nameSpace.name);
        for (var c : definitions.clubs) namespaces.add(c.typeName.nameSpace.name);
        for (var a : definitions.attachments) namespaces.add(a.typeName.nameSpace.name);
        for (var n : namespaces)
            check(found, target, "namespace", "the namespace " + n, n);

        for (DSMFunctionPool p : definitions.functionPools) {
            check(found, target, "pool", "the pool " + p.name, p.name);
            for (DSMFunction f : p.functions)
                function(found, target, p.name, f.prototype);
        }
        for (DSMAttachmentFunctionPool p : definitions.attachmentFunctionPools) {
            check(found, target, "pool", "the pool " + p.name, p.name);
            for (DSMAttachmentFunction f : p.functions)
                function(found, target, p.name, f.prototype);
        }

        for (var c : definitions.concepts)
            check(found, target, "type", "the concept " + c.typeName.representation(), c.typeName.name);
        for (var c : definitions.clubs)
            check(found, target, "type", "the club " + c.typeName.representation(), c.typeName.name);
        for (DSMEnumeration e : definitions.enumerations) {
            check(found, target, "type", "the enumeration " + e.typeName.representation(), e.typeName.name);
            for (DSMEnumerationCase c : e.members)
                check(found, target, "case", "the case " + e.typeName.representation() + "." + c.name, c.name);
        }
        for (DSMStructure s : definitions.structures) {
            check(found, target, "type", "the structure " + s.typeName.representation(), s.typeName.name);
            for (DSMStructureField f : s.fields)
                check(found, target, "field", "the field " + s.typeName.representation() + "." + f.name, f.name);
        }
        for (DSMAttachment a : definitions.attachments)
            check(found, target, "attachment", "the attachment " + a.typeName.representation(), a.typeName.name);

        return found;
    }

    private static void function(List<String> found, String target, String pool, DSMFunctionPrototype prototype) {
        check(found, target, "function", "the function " + pool + "." + prototype.name, prototype.name);
        for (DSMFunctionPrototypeParameter p : prototype.parameters)
            check(found, target, "parameter",
                  "the parameter " + p.name + " of " + pool + "." + prototype.name, p.name);
    }

    private static void check(List<String> found, String target, String kind, String what, String dsmName) {
        final var reserved = TemplateTool.targetNames().reserved(kind);
        if (reserved.isEmpty())
            return;
        final var spelled = TemplateTool.spell(dsmName);
        for (var written : new String[]{spelled, TemplateTool.snake(spelled), TemplateTool.lf(spelled)})
            if (reserved.contains(written)) {
                found.add(what + " is written " + written + " in " + target
                          + ", a name the template pack's own code takes there. Spell it otherwise for "
                          + target + ": [names." + target + ".rename] " + dsmName + " = \"...\" in kibo.toml, "
                          + "or kibo's --spell " + dsmName + "=...");
                return;
            }
    }
}
