package com.edam.minijson.exception;

public class JsonSerializationException extends RuntimeException {

    public JsonSerializationException(String message) {
        super(message);
    }

    public JsonSerializationException(Throwable e) {
        super(e);
    }
}
