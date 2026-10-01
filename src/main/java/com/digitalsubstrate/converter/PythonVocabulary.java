package com.digitalsubstrate.converter;

import com.digitalsubstrate.viper.dsm.DSMLexicon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * The `dsviper` Python binding. Numbers arrive as `int` or `float` whatever their width,
 * and the values Python has no equivalent for stay runtime values.
 */
final class PythonVocabulary implements BindingVocabulary {

    private final HashMap<String, String> leaves = new HashMap<>();

    PythonVocabulary() {
        leaves.put(DSMLexicon.Bool, "bool");
        for (var integer : new String[]{DSMLexicon.UInt8, DSMLexicon.UInt16, DSMLexicon.UInt32, DSMLexicon.UInt64,
                                        DSMLexicon.Int8, DSMLexicon.Int16, DSMLexicon.Int32, DSMLexicon.Int64})
            leaves.put(integer, "int");
        leaves.put(DSMLexicon.Float, "float");
        leaves.put(DSMLexicon.Double, "float");
        leaves.put(DSMLexicon.String, "str");
        leaves.put(DSMLexicon.Blob, "dsviper.ValueBlob");
        leaves.put(DSMLexicon.BlobId, "dsviper.ValueBlobId");
        leaves.put(DSMLexicon.CommitId, "dsviper.ValueCommitId");
        leaves.put(DSMLexicon.UUId, "dsviper.ValueUUId");
        leaves.put(TypeConverter.Any, "dsviper.ValueAny");
        leaves.put(DSMLexicon.Void, "None");
    }

    @Override
    public String leaf(String dsmPrimitive) {
        return leaves.get(dsmPrimitive);
    }

    @Override
    public String sequence(String element, long count) {
        final var members = new ArrayList<String>();
        for (var member = 0; member < count; member++)
            members.add(element);

        return String.format("tuple[%s]", String.join(", ", members));
    }

    // The binding's three views, and nothing else: a sequence, a mapping and an ordered
    // sequence are the three generic classes the runtime carries. They are enough, because a
    // container differs from another only by what it holds.

    @Override
    public String list(String element) {
        return String.format("Sequence[%s]", element);
    }

    @Override
    public String matrix(String element) {
        return String.format("Sequence[Sequence[%s]]", element);
    }

    /**
     * A tuple reads as a sequence of the union of its members.
     *
     * <p>Not {@code tuple[A, B]}, which would say more than is true: what the runtime hands
     * back is one view over a heterogeneous value, indexable but not positionally typed. The
     * spelling that a checker can rely on is the one that says a member is an A or a B, and
     * a reader who needs to know which reads the model.
     */
    @Override
    public String tuple(List<String> members) {
        return list(union(members));
    }

    @Override
    public String optional(String element) {
        return String.format("%s | None", element);
    }

    @Override
    public String map(String key, String element) {
        return String.format("Mapping[%s, %s]", key, element);
    }

    @Override
    public String ordered(String element) {
        return String.format("Ordered[%s]", element);
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
        return "typing.Any";
    }

    /** Every container shape is a class the package declares in its `containers` module. */
    @Override
    public String sequenceInput(String element) {
        return String.format("typing.Sequence[%s]", element);
    }

    @Override
    public String iterableInput(String element) {
        return String.format("typing.Iterable[%s]", element);
    }

    @Override
    public String mapInput(String key, String element) {
        return String.format("dict[%s, %s]", key, element);
    }

    @Override
    public String container(String generatedClass) {
        return "containers." + generatedClass;
    }
}
