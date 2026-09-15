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

    @Override
    public String list(String element) {
        return element + "[]";
    }

    @Override
    public String matrix(String element) {
        return element + "[][]";
    }

    @Override
    public String tuple(List<String> members) {
        return "[" + String.join(", ", members) + "]";
    }

    @Override
    public String optional(String element) {
        return element + " | null";
    }

    @Override
    public String map(String key, String element) {
        return String.format("Map<%s, %s>", key, element);
    }

    @Override
    public String ordered(String element) {
        return String.format("dsviper.XArray<%s>", element);
    }

    @Override
    public String union(List<String> members) {
        return String.join(" | ", new java.util.LinkedHashSet<>(members));
    }

    @Override
    public String any() {
        return "unknown";
    }
}
