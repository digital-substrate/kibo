// Copyright (c) Digital Substrate 2026, All rights reserved.

package com.digitalsubstrate.viper.stream;

public final class StreamToken {
    static final byte Bool = 0;

    static final byte UInt8 = 1;
    static final byte UInt16 = 2;
    static final byte UInt32 = 3;
    static final byte UInt64 = 4;

    static final byte Int8 = 5;
    static final byte Int16 = 6;
    static final byte Int32 = 7;
    static final byte Int64 = 8;

    static final byte Float = 9;
    static final byte Double = 10;

    static final byte BlobId = 11;
    static final byte CommitId = 12;
    static final byte Uuid = 13;

    static final byte String = 14;
    static final byte Blob = 15;

    static final byte Array_ = 16;
    static final byte LAST = 17;
}
