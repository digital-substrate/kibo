package com.digitalsubstrate.template;

import com.digitalsubstrate.viper.dsm.DSMLexicon;

public final class TemplatePythonType {

    private final String proxy;
    private final String typeSuffix;

    public TemplatePythonType(String proxy, String typeSuffix) {
        this.proxy = proxy;
        this.typeSuffix = typeSuffix;
    }

    public Boolean getUseProxy() {
        switch (proxy) {
            case DSMLexicon.Bool,
                 DSMLexicon.UInt8, DSMLexicon.UInt16, DSMLexicon.UInt32, DSMLexicon.UInt64,
                 DSMLexicon.Int8, DSMLexicon.Int16, DSMLexicon.Int32, DSMLexicon.Int64,
                 DSMLexicon.Float, DSMLexicon.Double,
                 DSMLexicon.BlobId, DSMLexicon.CommitId, DSMLexicon.UUId,
                 DSMLexicon.String, DSMLexicon.Blob,
                 "Any" -> {
                return false;
            }
            default -> {
                return true;
            }
        }
    }

    public String getProxy() {
        return proxy;
    }

    // Type
    public String getTypeSuffix() {
        return typeSuffix;
    }

    public String getType() {
        switch (proxy) {
            case DSMLexicon.Bool -> {
                return "bool";
            }
            case DSMLexicon.UInt8, DSMLexicon.UInt16, DSMLexicon.UInt32, DSMLexicon.UInt64,
                 DSMLexicon.Int8, DSMLexicon.Int16, DSMLexicon.Int32, DSMLexicon.Int64 -> {
                return "int";
            }
            case DSMLexicon.Float, DSMLexicon.Double -> {
                return "float";
            }
            case DSMLexicon.BlobId -> {
                return "dsviper.ValueBlobId";
            }
            case DSMLexicon.CommitId -> {
                return "dsviper.ValueCommitId";
            }
            case DSMLexicon.UUId -> {
                return "dsviper.ValueUUId";
            }
            case DSMLexicon.String -> {
                return "str";
            }
            case DSMLexicon.Blob -> {
                return "dsviper.ValueBlob";
            }
            case "Any" -> {
                return "dsviper.ValueAny";
            }
            case "void" -> {
                return "None";
            }
            default -> {
                return proxy;
            }
        }
    }
}
