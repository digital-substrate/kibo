// Copyright (c) Digital Substrate 2026, All rights reserved.

package com.digitalsubstrate.viper.stream;

import java.util.UUID;

public interface StreamReading {

    boolean readBool() throws Exception;

    byte readUInt8() throws Exception;

    short readUInt16() throws Exception;

    int readUInt32() throws Exception;

    long readUInt64() throws Exception;

    byte readInt8() throws Exception;

    short readInt16() throws Exception;

    int readInt32() throws Exception;

    long readInt64() throws Exception;

    float readFloat() throws Exception;

    double readDouble() throws Exception;

    UUID readUuid() throws Exception;

    String readString() throws Exception;

    byte[] readBlob() throws Exception;


}
