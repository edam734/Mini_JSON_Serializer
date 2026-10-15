package com.edam.minijson.exception;

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
