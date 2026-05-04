package com.digitalsubstrate.viper.dsm;

public final class DSMErrorsUnknownValue extends Exception {

    public DSMErrorsUnknownValue(String type) {
        super("Unknown value for type '" + type + "'.");
    }
}
