package com.digitalsubstrate.kibo;

import com.digitalsubstrate.template.TemplateTool;

import com.digitalsubstrate.converter.Target;
import com.digitalsubstrate.converter.TargetLayout;
import com.digitalsubstrate.converter.Converter;
import com.digitalsubstrate.viper.dsm.DSMDefinitions;
import com.digitalsubstrate.template.TemplateDefinitions;
import com.digitalsubstrate.template.TemplateStringRenderer;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroupFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public final class AppUtils {

    private AppUtils() {
    }

    // Templates
    public static Path outputFilePath(Path template) {
        final var filename = template.getFileName().toString();
        return Path.of(filename.substring(0, filename.lastIndexOf('.')));
    }

    public static ArrayList<Path> collectTemplates(Path template) {
        final var result = new ArrayList<Path>();
        if (Files.isDirectory(template)) {
            final var templateFile = new File(template.toString());
            final var files = templateFile.listFiles();
            if (files != null) {
                for (var file : files) {
                    if (file.getName().endsWith(".stg")) {
                        result.add(file.toPath());
                    }
                }
            }
        } else {
            result.add(template);
        }
        return result;
    }

    /** The entry a template declares, which is how it says what it is rendered for. */
    private static final String WHOLE_MODEL = "main";     // Template Model 1 and 2
    private static final String MODEL = "model";          // once, for the whole model
    private static final String PER_UNIT = "unit";        // once per namespace
    private static final String PER_POOL = "pool";        // once per function pool
    private static final String PER_ATTACHMENT_POOL = "attachment_pool";

    // Not "namespace": 58 templates already declare a sub-template of that name for
    // their loop body, and an entry cannot share a name with something that is not one.
    // "unit" is what the design calls a namespace's generated code anyway.

    /**
     * Whether a template declares this entry, taking the argument an entry takes.
     *
     * <p>The name alone is not enough. Template packs already use these words for their
     * own sub-templates — 58 declare {@code namespace(ns)} for a loop body, two declare
     * {@code pool(po)} — and rendering one of those with an argument it does not take
     * throws, which aborts the whole invocation and silently stops writing every file
     * that would have followed. A pack should not lose its render to a word it chose
     * before kibo reserved it.
     */
    private static boolean declares(STGroupFile group, String entry, String argument) {
        if (!group.isDefined(entry))
            return false;

        final var instance = group.getInstanceOf(entry);
        return instance != null
            && instance.impl != null
            && instance.impl.formalArguments != null
            && instance.impl.formalArguments.containsKey(argument);
    }

    private static STGroupFile group(Path template, RenderDiagnostics diagnostics) {
        final var group = new STGroupFile(template.toString());
        group.setListener(diagnostics);
        group.registerRenderer(String.class, new TemplateStringRenderer());
        return group;
    }

    private static String render(STGroupFile group, String entry, String argument, Object value) {
        final ST instance = group.getInstanceOf(entry);
        instance.add(argument, value);
        return instance.render();
    }

    private static void save(Target target, TargetLayout.Scope scope, String model, String unit, Path template,
                             Path output, String code, boolean debug) throws Exception {
        final var filename = target.layout.outputFileName(scope, model, unit, outputFilePath(template).toString());
        final var filePath = Paths.get(output.toString(), filename);
        if (debug)
            System.out.printf("Save %s%n", filePath);

        FileUtils.saveSource(code, filePath);
    }

    /**
     * Render one template file, once per thing it says it is for.
     *
     * <p>A template declares its scope by which entry it defines, which is the only
     * thing kibo can ask: it never sees a template pack, only whatever {@code -t}
     * points at, so there is no manifest to consult and nothing to keep in step.
     *
     * <p>{@code main(m)} renders the whole model into one file, as Template Model 1 and
     * 2 always have. {@code model(m)} does the same and says so. {@code unit(u)} renders
     * once per namespace, into one file each — and a template may declare both, because
     * most of them are both: the primitives and the container functions of
     * {@code ValueHexdigest} belong to the model, and only its concepts and structures
     * belong to a unit.
     *
     * <p>{@code pool(p)} and {@code attachment_pool(p)} render once per function pool and
     * per attachment function pool.
     *
     * @return whether the template declared an entry; the caller decides what a template
     * declaring none means.
     */
    private static boolean renderAndSave(Target target, TemplateDefinitions templateDefinitions, Path template, Path output, boolean debug) throws Exception {
        if (debug)
            System.out.println("Render " + template);

        final var diagnostics = new RenderDiagnostics(template);
        final var group = group(template, diagnostics);
        var rendered = false;

        // main and model write the same file: declaring both would render the second over the first.
        if (declares(group, WHOLE_MODEL, "m") && declares(group, MODEL, "m"))
            throw new Exception(template + " declares both main(m) and model(m), which render to the same file: keep one.");

        for (var entry : new String[]{WHOLE_MODEL, MODEL})
            if (declares(group, entry, "m")) {
                save(target, TargetLayout.Scope.MODEL, templateDefinitions.getNamespace(), templateDefinitions.getNamespace(), template, output,
                     render(group, entry, "m", templateDefinitions), debug);
                rendered = true;
            }

        if (declares(group, PER_UNIT, "u")) {
            for (var nameSpace : templateDefinitions.nameSpaces)
                save(target, TargetLayout.Scope.UNIT, templateDefinitions.getNamespace(), nameSpace.getName(), template, output,
                     render(group, PER_UNIT, "u", nameSpace), debug);
            rendered = true;
        }

        // A pool is a unit too — a namespace holding only functions — but it is a
        // different collection to walk, and a template written for one would read
        // accessors the other does not have. Separate entries rather than one that
        // renders plausibly wrong output for half its inputs.
        if (declares(group, PER_POOL, "p")) {
            for (var pool : templateDefinitions.functionPools)
                save(target, TargetLayout.Scope.UNIT, templateDefinitions.getNamespace(), pool.getName(), template, output,
                     render(group, PER_POOL, "p", pool), debug);
            rendered = true;
        }

        if (declares(group, PER_ATTACHMENT_POOL, "p")) {
            for (var pool : templateDefinitions.attachmentFunctionPools)
                save(target, TargetLayout.Scope.UNIT, templateDefinitions.getNamespace(), pool.getName(), template, output,
                     render(group, PER_ATTACHMENT_POOL, "p", pool), debug);
            rendered = true;
        }

        diagnostics.summarize();
        return rendered;
    }

    private static final String ENTRIES = "main(m), model(m), unit(u), pool(p) or attachment_pool(p)";

    /**
     * Render what {@code -t} points at.
     *
     * <p>A file {@code -t} names that declares no entry is an error rather than an empty
     * file: StringTemplate answers a missing template with the empty string, and a generator
     * that writes a plausible short file is worse than one that stops. In a directory, such a
     * file is one the others import — a pack's banner — and is skipped; a directory where no
     * file declares an entry is the same error.
     */
    public static void renderAndSave(Target target, TemplateDefinitions templateDefinitions, Path template, ArrayList<Path> templates,
                                     Path output, boolean debug) throws Exception {
        Files.createDirectories(output);
        var any = false;
        for (var file : templates)
            any |= renderAndSave(target, templateDefinitions, file, output, debug);
        if (!any) {
            final var what = Files.isDirectory(template) ? "no template in " + template + " declares an entry" : template + " declares no entry";
            throw new Exception(what + ": expected " + ENTRIES + ".");
        }
    }

    /**
     * Refuse a model that would write two different files to one path.
     *
     * <p>A template may render both once per namespace and once for the model — most do —
     * and the two outputs are told apart only by the name they are prefixed with. When a
     * namespace of the model carries the model's own name, the prefixes are equal, and the
     * second write silently replaces the first: whichever of the unit or the model-wide
     * artefact is rendered last is the one that survives, and nothing says so.
     *
     * <p>Caught here rather than in a layout, because no naming scheme fixes it. The two
     * files describe different things that happen to be called the same, and only the
     * model can say which one should be renamed.
     */
    private static void checkNamespaceDoesNotShadowModel(TemplateDefinitions definitions) throws Exception {
        for (var nameSpace : definitions.nameSpaces)
            if (nameSpace.getName().equals(definitions.getNamespace()))
                throw new Exception(String.format(
                    "the model is generated as '%s' and declares a namespace of that name, so its "
                    + "artefacts and that namespace's would be written to the same files. "
                    + "Rename the namespace, or generate the model under another name (-n).",
                    definitions.getNamespace()));
    }

    /**
     * In C++ the namespaces of the DSM and the function pools are all written under the model's
     * name (-n), each in lower snake case -- the rule kibo applies itself. Two of them that the
     * rule spells alike would merge into one C++ namespace: a pool and a namespace both called
     * Tools, or two pools. Refused here, when the model is read, rather than as a redefinition deep
     * in a compile.
     *
     * <p>Only what follows from kibo's own rule is checked. The names a template pack gives its
     * model-wide code (a codec, a pool of attachments...) are the pack's, which kibo does not know:
     * a clash with them is the pack's to declare and the project's to resolve.
     */
    private static void checkNamesUnderModel(TemplateDefinitions definitions) throws Exception {
        final var seen = new java.util.HashMap<String, String>();
        final var names = new java.util.ArrayList<String[]>();
        for (var nameSpace : definitions.nameSpaces)
            names.add(new String[]{"namespace", nameSpace.getName()});
        for (var pool : definitions.functionPools)
            names.add(new String[]{"function pool", pool.getName()});
        for (var pool : definitions.attachmentFunctionPools)
            names.add(new String[]{"attachment function pool", pool.getName()});
        for (var name : names) {
            final var cpp = TemplateTool.lsc(name[1]);
            final var previous = seen.putIfAbsent(cpp, name[0] + " '" + name[1] + "'");
            if (previous != null)
                throw new Exception(String.format(
                    "the %s '%s' and the %s are both written '%s::%s' in C++. Rename one.",
                    name[0], name[1], previous, definitions.getNamespace(), cpp));
        }
    }

    public static void generate(Target target, String generated, DSMDefinitions dsmDefinitions, String namespace,
                                Path template, Path output, boolean debug) throws Exception {
        final var templateDefinitions = new Converter(generated, dsmDefinitions, namespace, target).convert();
        checkNamespaceDoesNotShadowModel(templateDefinitions);
        if (target == Target.CPP)
            checkNamesUnderModel(templateDefinitions);
        renderAndSave(target, templateDefinitions, template, AppUtils.collectTemplates(template), output, debug);
    }
}
