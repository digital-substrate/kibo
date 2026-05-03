// Copyright (c) Digital Substrate 2026, All rights reserved.
package com.digitalsubstrate.viper.dsm;

import com.digitalsubstrate.viper.NameSpace;
import com.digitalsubstrate.viper.TypeName;

public final class DSMTypeReference extends DSMType {

    public final TypeName typeName;
    public final DSMTypeReferenceDomain domain;

    public static final DSMTypeReference Any = new DSMTypeReference(
            new TypeName(DSMLexicon.Any), DSMTypeReferenceDomain.ANY);


    public static final DSMTypeReference AnyConcept = new DSMTypeReference(
            new TypeName(DSMLexicon.AnyConcept), DSMTypeReferenceDomain.ANY_CONCEPT);

    public static final DSMTypeReference Void = new DSMTypeReference(
            new TypeName(DSMLexicon.Void), DSMTypeReferenceDomain.PRIMITIVE);

    public static final DSMTypeReference Bool = new DSMTypeReference(
            new TypeName(DSMLexicon.Bool), DSMTypeReferenceDomain.PRIMITIVE);

    public static final DSMTypeReference UInt8 = new DSMTypeReference(
            new TypeName(DSMLexicon.UInt8), DSMTypeReferenceDomain.PRIMITIVE);

    public static final DSMTypeReference UInt16 = new DSMTypeReference(
            new TypeName(DSMLexicon.UInt16), DSMTypeReferenceDomain.PRIMITIVE);

    public static final DSMTypeReference UInt32 = new DSMTypeReference(
            new TypeName(DSMLexicon.UInt32), DSMTypeReferenceDomain.PRIMITIVE);

    public static final DSMTypeReference UInt64 = new DSMTypeReference(
            new TypeName(DSMLexicon.UInt64), DSMTypeReferenceDomain.PRIMITIVE);

    public static final DSMTypeReference Int8 = new DSMTypeReference(
            new TypeName(DSMLexicon.Int8), DSMTypeReferenceDomain.PRIMITIVE);

    public static final DSMTypeReference Int16 = new DSMTypeReference(
            new TypeName(DSMLexicon.Int16), DSMTypeReferenceDomain.PRIMITIVE);

    public static final DSMTypeReference Int32 = new DSMTypeReference(
            new TypeName(DSMLexicon.Int32), DSMTypeReferenceDomain.PRIMITIVE);

    public static final DSMTypeReference Int64 = new DSMTypeReference(
            new TypeName(DSMLexicon.Int64), DSMTypeReferenceDomain.PRIMITIVE);

    public static final DSMTypeReference Float = new DSMTypeReference(
            new TypeName(DSMLexicon.Float), DSMTypeReferenceDomain.PRIMITIVE);

    public static final DSMTypeReference Double = new DSMTypeReference(
            new TypeName(DSMLexicon.Double), DSMTypeReferenceDomain.PRIMITIVE);

    public static final DSMTypeReference BlobId = new DSMTypeReference(
            new TypeName(DSMLexicon.BlobId), DSMTypeReferenceDomain.PRIMITIVE);

    public static final DSMTypeReference CommitId = new DSMTypeReference(
            new TypeName(DSMLexicon.CommitId), DSMTypeReferenceDomain.PRIMITIVE);

    public static final DSMTypeReference Uuid = new DSMTypeReference(
            new TypeName(DSMLexicon.UUId), DSMTypeReferenceDomain.PRIMITIVE);

    public static final DSMTypeReference String = new DSMTypeReference(
            new TypeName(DSMLexicon.String), DSMTypeReferenceDomain.PRIMITIVE);

    public static final DSMTypeReference Blob = new DSMTypeReference(
            new TypeName(DSMLexicon.Blob), DSMTypeReferenceDomain.PRIMITIVE);

    public DSMTypeReference(TypeName typeName, DSMTypeReferenceDomain domain) {
        this.typeName = typeName;
        this.domain = domain;
    }

    public static DSMTypeReference byName(String name) throws Exception {
        switch (name) {
            case DSMLexicon.Void -> {
                return Void;
            }

            case DSMLexicon.Bool -> {
                return Bool;
            }

            case DSMLexicon.UInt8 -> {
                return UInt8;
            }
            case DSMLexicon.UInt16 -> {
                return UInt16;
            }
            case DSMLexicon.UInt32 -> {
                return UInt32;
            }
            case DSMLexicon.UInt64 -> {
                return UInt64;
            }

            case DSMLexicon.Int8 -> {
                return Int8;
            }
            case DSMLexicon.Int16 -> {
                return Int16;
            }
            case DSMLexicon.Int32 -> {
                return Int32;
            }
            case DSMLexicon.Int64 -> {
                return Int64;
            }

            case DSMLexicon.Float -> {
                return Float;
            }
            case DSMLexicon.Double -> {
                return Double;
            }

            case DSMLexicon.BlobId -> {
                return BlobId;
            }

            case DSMLexicon.CommitId -> {
                return CommitId;
            }

            case DSMLexicon.UUId -> {
                return Uuid;
            }
            case DSMLexicon.String -> {
                return String;
            }
            case DSMLexicon.Blob -> {
                return Blob;
            }
        }

        throw new DSMErrorsUnknownPrimitiveName(name);
    }

    public String representation() {
        return typeName.representation();
    }

    public String representationIn(NameSpace nameSpace) {
        return typeName.representationIn(nameSpace);
    }
}
