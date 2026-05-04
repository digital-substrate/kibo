package com.digitalsubstrate.viper.stream;

public final class StreamErrorsUnexpectedToken extends Exception {

    public StreamErrorsUnexpectedToken(byte value, byte expected) {
        super("Expected token '" + String.valueOf(expected) + "', got '" + String.valueOf(value) + "'.");
    }
}
