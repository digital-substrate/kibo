package com.digitalsubstrate.converter;

/**
 * Two types of the model written alike, or a project's name for a type the model does not have.
 * Not a defect of kibo: the message says which directive settles it, and kibo prints it as is.
 */
public final class TypeNamesException extends Exception {
    TypeNamesException(String message) {
        super(message);
    }
}
