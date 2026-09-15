package com.digitalsubstrate.kibo;

import com.digitalsubstrate.converter.Target;
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

    private static void save(Target target, String unit, Path template, Path output, String code, boolean debug)
            throws Exception {
        final var filename = target.layout.outputFileName(unit, outputFilePath(template).toString());
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
     * <p>A template declaring none of the three is an error rather than an empty file.
     * StringTemplate answers a missing template with the empty string, and a generator
     * that writes a plausible short file is worse than one that stops.
     */
    public static void renderAndSave(Target target, TemplateDefinitions templateDefinitions, Path template, Path output, boolean debug) throws Exception {
        if (debug)
            System.out.println("Render " + template);

        final var diagnostics = new RenderDiagnostics(template);
        final var group = group(template, diagnostics);
        var rendered = false;

        for (var entry : new String[]{WHOLE_MODEL, MODEL})
            if (declares(group, entry, "m")) {
                save(target, templateDefinitions.getNamespace(), template, output,
                     render(group, entry, "m", templateDefinitions), debug);
                rendered = true;
            }

        if (declares(group, PER_UNIT, "u")) {
            for (var nameSpace : templateDefinitions.nameSpaces)
                save(target, nameSpace.getName(), template, output,
                     render(group, PER_UNIT, "u", nameSpace), debug);
            rendered = true;
        }

        // A pool is a unit too — a namespace holding only functions — but it is a
        // different collection to walk, and a template written for one would read
        // accessors the other does not have. Separate entries rather than one that
        // renders plausibly wrong output for half its inputs.
        if (declares(group, PER_POOL, "p")) {
            for (var pool : templateDefinitions.functionPools)
                save(target, pool.getName(), template, output,
                     render(group, PER_POOL, "p", pool), debug);
            rendered = true;
        }

        if (declares(group, PER_ATTACHMENT_POOL, "p")) {
            for (var pool : templateDefinitions.attachmentFunctionPools)
                save(target, pool.getName(), template, output,
                     render(group, PER_ATTACHMENT_POOL, "p", pool), debug);
            rendered = true;
        }

        diagnostics.summarize();

        if (!rendered)
            throw new Exception(template + " declares no entry: expected main(m), model(m) or namespace(u).");
    }

    public static void renderAndSave(Target target, TemplateDefinitions templateDefinitions, ArrayList<Path> templates, Path output, boolean debug) throws Exception {
        Files.createDirectories(output);
        for (var template : templates)
            renderAndSave(target, templateDefinitions, template, output, debug);
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

    public static void generate(Target target, String generated, DSMDefinitions dsmDefinitions, String namespace,
                                Path template, Path output, boolean debug) throws Exception {
        final var templateDefinitions = new Converter(generated, dsmDefinitions, namespace, target).convert();
        checkNamespaceDoesNotShadowModel(templateDefinitions);
        renderAndSave(target, templateDefinitions, AppUtils.collectTemplates(template), output, debug);
    }
}
