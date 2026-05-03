// Copyright (c) Digital Substrate 2026, All rights reserved.

package com.digitalsubstrate.viper.stream;

public interface StreamDecoding extends StreamReading {

    byte[] data();

    int offset();

    boolean hasMore();

    void rewind();

}
