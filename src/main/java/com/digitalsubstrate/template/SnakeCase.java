package com.digitalsubstrate.template;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * The snake_case of a static name in a generated package: a Python field, method, parameter,
 * module, and the package directories both packages share.
 *
 * <p>A name with no capital is kept as written. An underscore the author wrote is kept, and each
 * segment between two is converted on its own. Inside a segment a boundary falls:
 * <ul>
 *   <li>before a capital that follows a lowercase letter: {@code doc|UInt8};</li>
 *   <li>before the last capital of a run followed by a lowercase letter: {@code RGB|Profile},
 *       {@code P3D|Bridge} — unless that lowercase is a plural {@code s} ending the word:
 *       {@code user|IDs};</li>
 *   <li>before a capital that follows a digit and is followed by a lowercase letter:
 *       {@code vec3|Curves};</li>
 *   <li>before digits that follow a lowercase letter when they are three or more — a number of
 *       their own, {@code size|1024} — or a unit, a run followed by a lone capital:
 *       {@code render|2D|Attributes}. One or two digits stay with their word: {@code int8},
 *       {@code vec3}, {@code md4}.</li>
 * </ul>
 *
 * <p>An atom is never split and is lowercased whole. The DSM's own vocabulary is built in —
 * {@code UInt}, {@code UUId}, {@code XArray} — and a project adds the words only its author can
 * split ({@code IPv4}, {@code YCoCg}); a project may also rename a whole name.
 *
 * <p>A projection that lands on a word Python reserves — a keyword, or a {@code __future__}
 * feature such as {@code annotations}, which a generated module imports — takes a trailing
 * underscore, as PEP 8 recommends: a namespace {@code Annotations} gives the module
 * {@code annotations_}, a field {@code from} the attribute {@code from_}. The uppercase form needs
 * none.
 *
 * <p>The rule was measured on every DSM model at hand rather than chosen on hard cases: of 3,604
 * distinct names it changes 18 against the previous one, each for the better, and collides
 * nowhere. It is not the wire's: names the runtime computes ({@code Definitions.inject()}) keep
 * {@link TemplateTool#lsc}, which viper specifies with its own vectors.
 */
public final class SnakeCase {

    public static final List<String> BUILTIN_ATOMS = List.of("UInt", "UUId", "XArray");

    /** Python's keywords and __future__ features: a name spelled as one takes a trailing underscore. */
    static final Set<String> RESERVED = Set.of(
        "and", "as", "assert", "async", "await", "break", "class", "continue", "def", "del", "elif",
        "else", "except", "finally", "for", "from", "global", "if", "import", "in", "is", "lambda",
        "nonlocal", "not", "or", "pass", "raise", "return", "try", "while", "with", "yield",
        "annotations", "absolute_import", "division", "generators", "generator_stop", "nested_scopes",
        "print_function", "unicode_literals", "with_statement", "barry_as_FLUFL");

    private final List<String> atoms;
    private final Map<String, String> renames;
    private final Set<String> declared;

    private SnakeCase(List<String> atoms, Map<String, String> renames, Set<String> declared) {
        final var all = new ArrayList<>(BUILTIN_ATOMS);
        for (var atom : atoms)
            if (!atom.isEmpty() && !all.contains(atom))
                all.add(atom);
        all.sort(Comparator.comparingInt(String::length).reversed());
        this.atoms = List.copyOf(all);
        this.renames = Map.copyOf(renames);
        this.declared = Set.copyOf(declared);
    }

    public static SnakeCase standard() {
        return new SnakeCase(List.of(), Map.of(), Set.of());
    }

    public static SnakeCase of(List<String> atoms, Map<String, String> renames) {
        return new SnakeCase(atoms, renames, Set.of());
    }

    /**
     * With the names the template pack's own code takes in this target ({@code --reserve}): a DSM
     * name spelled as one of them takes a trailing underscore, as a Python keyword does, so that it
     * does not take the place of the pack's — a field {@code wrap_value} beside the proxy's method.
     */
    public static SnakeCase of(List<String> atoms, Map<String, String> renames, Set<String> declared) {
        return new SnakeCase(atoms, renames, declared);
    }

    /** Whether the pack declared this name for its own code. */
    public boolean isDeclared(String name) {
        return declared.contains(name);
    }

    public String of(String name) {
        final var renamed = renames.get(name);
        if (renamed != null)
            return renamed;

        final var projected = project(name);
        return RESERVED.contains(projected) || declared.contains(projected) ? projected + "_" : projected;
    }

    /**
     * The name of one of the pack's own artefacts ({@code containers}, {@code data}): the names the
     * pack declares are its own, so they are not escaped here.
     */
    public String ofArtefact(String name) {
        final var projected = project(name);
        return RESERVED.contains(projected) ? projected + "_" : projected;
    }

    public String upper(String name) {
        final var renamed = renames.get(name);
        return (renamed != null ? renamed : project(name)).toUpperCase(Locale.ROOT);
    }

    private String project(String name) {
        if (name.chars().noneMatch(Character::isUpperCase))
            return name;

        final var segments = name.split("_", -1);
        final var out = new StringBuilder(name.length() + 8);
        for (int k = 0; k < segments.length; ++k) {
            if (k > 0)
                out.append('_');
            out.append(segment(segments[k]));
        }
        return out.toString();
    }

    // The class each character is read as: an atom reads as one capitalised word, so the rules
    // place a boundary around it and none inside it.
    private static final byte UPPER = 1, LOWER = 2, DIGIT = 3, OTHER = 0;

    private String segment(String s) {
        final int n = s.length();
        final var cls = new byte[n];
        final var inside = new boolean[n];      // a position inside an atom, after its first letter
        for (int i = 0; i < n; ++i) {
            final char c = s.charAt(i);
            cls[i] = Character.isUpperCase(c) ? UPPER : Character.isLowerCase(c) ? LOWER
                   : Character.isDigit(c) ? DIGIT : OTHER;
        }
        for (int i = 0; i < n; ) {
            final var atom = atomAt(s, i, cls);
            if (atom == null) {
                ++i;
                continue;
            }
            cls[i] = UPPER;
            for (int j = i + 1; j < i + atom.length(); ++j) {
                cls[j] = LOWER;
                inside[j] = true;
            }
            i += atom.length();
        }

        final var out = new StringBuilder(n + 4);
        for (int i = 0; i < n; ++i) {
            if (i > 0 && !inside[i] && boundary(s, cls, i))
                out.append('_');
            out.append(s.charAt(i));
        }
        return out.toString().toLowerCase(Locale.ROOT);
    }

    private String atomAt(String s, int i, byte[] cls) {
        if (i > 0 && cls[i - 1] == UPPER)
            return null;
        for (var atom : atoms) {
            if (!s.startsWith(atom, i))
                continue;
            final int end = i + atom.length();
            if (end < s.length() && cls[end] == LOWER)
                continue;
            return atom;
        }
        return null;
    }

    private static boolean boundary(String s, byte[] cls, int i) {
        final int n = cls.length;
        final boolean upperHere = cls[i] == UPPER;
        final boolean lowerNext = i + 1 < n && cls[i + 1] == LOWER;

        if (upperHere && cls[i - 1] == LOWER)
            return true;
        if (upperHere && cls[i - 1] == UPPER && lowerNext)
            return !pluralS(s, cls, i + 1);
        if (upperHere && cls[i - 1] == DIGIT && lowerNext)
            return true;
        if (cls[i] == DIGIT && cls[i - 1] == LOWER) {
            int j = i;
            while (j < n && cls[j] == DIGIT)
                ++j;
            final boolean unit = j < n && cls[j] == UPPER && !(j + 1 < n && cls[j + 1] == LOWER);
            return j - i >= 3 || unit;
        }
        return false;
    }

    // The lowercase at i is a lone plural s ending the word: "IDs", not "IDsomething".
    private static boolean pluralS(String s, byte[] cls, int i) {
        return s.charAt(i) == 's' && !(i + 1 < cls.length && cls[i + 1] == LOWER);
    }
}
