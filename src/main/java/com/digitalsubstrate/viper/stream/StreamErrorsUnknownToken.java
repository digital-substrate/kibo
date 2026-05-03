// Copyright (c) Digital Substrate 2026, All rights reserved.

package com.digitalsubstrate.viper.stream;

public final class StreamErrorsUnknownToken extends Exception {

    public StreamErrorsUnknownToken(byte token) {
        super("Unknown token '" + String.valueOf(token) + "'.");
    }
}
