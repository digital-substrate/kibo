package com.digitalsubstrate.converter;

import com.digitalsubstrate.viper.dsm.DSMLexicon;

import java.util.ArrayList;
import java.util.HashMap;

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
}
