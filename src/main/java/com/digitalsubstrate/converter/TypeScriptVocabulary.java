package com.digitalsubstrate.converter;

import com.digitalsubstrate.viper.dsm.DSMLexicon;

import java.util.HashMap;
import java.util.List;

/**
 * The `@digitalsubstrate/dsviper` Node binding. Widths that exceed what a JavaScript
 * number holds exactly arrive as `bigint`; the values TypeScript has no equivalent for
 * stay runtime values.
 *
 * <p>A fixed-size sequence has no TypeScript spelling that carries its size, so it is
 * written as an array of its element.
 */
final class TypeScriptVocabulary implements BindingVocabulary {

    private final HashMap<String, String> leaves = new HashMap<>();

    TypeScriptVocabulary() {
        leaves.put(DSMLexicon.Bool, "boolean");
        for (var number : new String[]{DSMLexicon.UInt8, DSMLexicon.UInt16, DSMLexicon.UInt32,
                                       DSMLexicon.Int8, DSMLexicon.Int16, DSMLexicon.Int32,
                                       DSMLexicon.Float, DSMLexicon.Double})
            leaves.put(number, "number");
        leaves.put(DSMLexicon.UInt64, "bigint");
        leaves.put(DSMLexicon.Int64, "bigint");
        leaves.put(DSMLexicon.String, "string");
        leaves.put(DSMLexicon.Blob, "dsviper.ValueBlob");
        leaves.put(DSMLexicon.BlobId, "dsviper.ValueBlobId");
        leaves.put(DSMLexicon.CommitId, "dsviper.ValueCommitId");
        leaves.put(DSMLexicon.UUId, "dsviper.ValueUUId");
        leaves.put(TypeConverter.Any, "dsviper.ValueAny");
        leaves.put(DSMLexicon.Void, "void");

    }

    @Override
    public String leaf(String dsmPrimitive) {
        return leaves.get(dsmPrimitive);
    }

    @Override
    public String sequence(String element, long count) {
        return element + "[]";
    }

    // The binding's three views, by their real names. An annotation is worth something only
    // if it names a class that exists: `Map<K, V>` would be wrong here, because a JavaScript
    // `Map` indexes by identity where a runtime mapping indexes by value. Confusing the two
    // would write code that compiles and loses entries.

    @Override
    public String list(String element) {
        return String.format("Sequence<%s>", element);
    }

    @Override
    public String matrix(String element) {
        return String.format("Sequence<Sequence<%s>>", element);
    }

    /**
     * A tuple reads as a sequence of the union of its members.
     *
     * <p>Not {@code [A, B]}, which would say more than is true: what the runtime hands back
     * is one view over a heterogeneous value, indexable but not positionally typed.
     */
    @Override
    public String tuple(List<String> members) {
        return list(union(members));
    }

    /**
     * {@code undefined} and not {@code null}.
     *
     * <p>A missing document reads as {@code undefined} because that is what an absent value
     * is in this language — what a property with no value, an array past its end, and a
     * lookup that found nothing all return. {@code null} would be a second spelling of
     * absence, and the two are not interchangeable under {@code strictNullChecks}.
     */
    @Override
    public String optional(String element) {
        return element.endsWith(" | undefined") ? element : element + " | undefined";
    }

    @Override
    public String map(String key, String element) {
        return String.format("Mapping<%s, %s>", key, element);
    }

    @Override
    public String ordered(String element) {
        return String.format("Ordered<%s>", element);
    }

    @Override
    public String union(List<String> members) {
        return String.join(" | ", new java.util.LinkedHashSet<>(members));
    }

    @Override
    public String any() {
        return "AnyValue";
    }

    @Override
    public String anyInput() {
        return "unknown";
    }

    @Override
    public String sequenceInput(String element) {
        return String.format("readonly (%s)[]", element);
    }

    /**
     * The list of its elements: the Node binding the templates require (>= 1.2.14) decodes a
     * written xarray from them, as Python does.
     */
    @Override
    public String orderedInput(String element) {
        return sequenceInput(element);
    }

    @Override
    public String iterableInput(String element) {
        return String.format("readonly (%s)[] | ReadonlySet<%s>", element, element);
    }

    @Override
    public String mapInput(String key, String element) {
        return String.format("ReadonlyMap<%s, %s> | readonly (readonly [%s, %s])[]", key, element, key, element);
    }

    /** Every container shape is a class the package declares in its `containers` module. */
    @Override
    public String container(String generatedClass) {
        return "containers." + generatedClass;
    }
}
