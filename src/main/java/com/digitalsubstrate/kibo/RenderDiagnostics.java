package com.digitalsubstrate.kibo;

import org.stringtemplate.v4.STErrorListener;
import org.stringtemplate.v4.misc.STMessage;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Reports what StringTemplate would otherwise swallow.
 *
 * <p>A template that reads an accessor the Template Model does not carry renders the
 * empty string. The render succeeds, the output is silently short, and nothing reaches
 * stderr — so a template written against an older model keeps producing files, minus
 * the parts that no longer resolve. That is not a failure mode anyone can migrate
 * against, which is why every render installs this listener.
 *
 * <p>Diagnostics are warnings, not errors: the file is still written and kibo still
 * exits zero. Whether the output is acceptable is the operator's call; this only makes
 * sure they are told where to look.
 */
final class RenderDiagnostics implements STErrorListener {

    private final Path template;
    private final Map<String, Integer> occurrences = new LinkedHashMap<>();

    RenderDiagnostics(Path template) {
        this.template = template;
    }

    /**
     * Prints each distinct diagnostic once, with how many times it fired. Silent when
     * the render was clean. Called once per render.
     */
    void summarize() {
        for (var occurrence : occurrences.entrySet()) {
            final var times = occurrence.getValue() == 1 ? "" : String.format(" (%d times)", occurrence.getValue());
            System.err.printf("kibo: %s: %s%s%n", template, occurrence.getKey(), times);
        }
    }

    /**
     * An expression inside a loop reports once per iteration, and StringTemplate
     * appends the whole stack trace to a message carrying a cause. Keep the first
     * line, which names the template context and what did not resolve, and count the
     * repeats rather than printing them.
     */
    private void report(STMessage message) {
        final var text = String.valueOf(message);
        final var end = text.indexOf('\n');
        occurrences.merge(end < 0 ? text : text.substring(0, end), 1, Integer::sum);
    }

    @Override
    public void compileTimeError(STMessage message) {
        report(message);
    }

    @Override
    public void runTimeError(STMessage message) {
        report(message);
    }

    @Override
    public void IOError(STMessage message) {
        report(message);
    }

    @Override
    public void internalError(STMessage message) {
        report(message);
    }
}
