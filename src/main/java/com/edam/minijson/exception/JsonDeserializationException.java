package com.edam.minijson.exception;

public class JsonDeserializationException extends RuntimeException {

    public JsonDeserializationException(String message) {
        super(message);
    }

    public JsonDeserializationException(Throwable e) {
        super(e);
    }

    public JsonDeserializationException(String message, Throwable e) {
        super(message, e);
    }
}
