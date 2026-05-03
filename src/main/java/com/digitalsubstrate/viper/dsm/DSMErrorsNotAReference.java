// Copyright (c) Digital Substrate 2026, All rights reserved.

package com.digitalsubstrate.viper.dsm;

public final class DSMErrorsNotAReference extends Exception {

    public DSMErrorsNotAReference() {
        super("Type is not a TypeReference.");
    }
}
