package com.digitalsubstrate.viper.dsm;

public final class DSMErrorsUnknownPrimitiveName extends Exception {

    public DSMErrorsUnknownPrimitiveName(String name) {
        super("Unknown primitive name '" + name + "'.");
    }
}
