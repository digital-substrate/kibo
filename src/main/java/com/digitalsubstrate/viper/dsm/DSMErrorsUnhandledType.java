// Copyright (c) Digital Substrate 2026, All rights reserved.

package com.digitalsubstrate.viper.dsm;

public final class DSMErrorsUnhandledType extends Exception {

    public DSMErrorsUnhandledType(String typeBase) {
        super("Unhandled type for '" + typeBase + "'.");
    }
}
