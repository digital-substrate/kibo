// Copyright (c) Digital Substrate 2026, All rights reserved.

package com.digitalsubstrate.viper.stream;

public final class StreamErrorsPrematureEndOfStream extends Exception {

    public StreamErrorsPrematureEndOfStream() {
        super("Prematurely reaching the end of the stream.");
    }

}
