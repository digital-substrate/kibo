package com.digitalsubstrate.viper.dsm;

import java.util.ArrayList;

public final class DSMDefinitionsCheckerReport {

    public final ArrayList<String> errors;

    public DSMDefinitionsCheckerReport() {
        this.errors = new ArrayList<>();
    }

    public boolean hasError() {
        return !errors.isEmpty();
    }

    public void add(String message) {
        errors.add("Error:" + message);
    }

    public void bail() {
        for (var error : errors) {
            System.out.println(error);
        }
    }
}
