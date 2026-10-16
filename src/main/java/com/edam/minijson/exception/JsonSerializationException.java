package com.edam.minijson.exception;

/**
 * Exception thrown when an error occurs during JSON serialization, such as an unsupported Java type
 * or an inaccessible field.
 */
public class JsonSerializationException extends RuntimeException {

    public JsonSerializationException(String message) {
        super(message);
    }

    public JsonSerializationException(Throwable e) {
        super(e);
    }

    public JsonSerializationException(String message, Throwable e) {
        super(message, e);
    }
}
