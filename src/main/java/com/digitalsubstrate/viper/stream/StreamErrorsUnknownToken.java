package com.digitalsubstrate.viper.stream;

public final class StreamErrorsUnknownToken extends Exception {

    public StreamErrorsUnknownToken(byte token) {
        super("Unknown token '" + String.valueOf(token) + "'.");
    }
}
