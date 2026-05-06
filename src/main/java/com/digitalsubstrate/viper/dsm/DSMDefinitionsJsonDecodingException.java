package com.digitalsubstrate.viper.dsm;

public final class DSMDefinitionsJsonDecodingException extends Exception {

    public DSMDefinitionsJsonDecodingException(String message) {
        super(message);
    }

    public DSMDefinitionsJsonDecodingException(String message, Throwable cause) {
        super(message, cause);
    }

    public static DSMDefinitionsJsonDecodingException expectedType(String path, String type) {
        return new DSMDefinitionsJsonDecodingException(
                "expected json " + type + " [" + path + "].");
    }

    public static DSMDefinitionsJsonDecodingException expectedMember(String path, String member) {
        return new DSMDefinitionsJsonDecodingException(
                "expected member " + member + " [" + path + "].");
    }

    public static DSMDefinitionsJsonDecodingException expectedMemberType(String path, String member, String type) {
        return new DSMDefinitionsJsonDecodingException(
                "expected member " + member + " of type " + type + " [" + path + "].");
    }

    public static DSMDefinitionsJsonDecodingException unknownValue(String path, String type, String value) {
        return new DSMDefinitionsJsonDecodingException(
                "unknown value " + value + " of type " + type + " [" + path + "].");
    }
}
