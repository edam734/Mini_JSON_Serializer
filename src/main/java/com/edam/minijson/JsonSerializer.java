package com.edam.minijson;

import com.edam.minijson.internal.SerializationEngine;

public class JsonSerializer {

    public static String toJson(Object obj) {
        SerializationEngine serializationEngine = new SerializationEngine();
        return serializationEngine.serialize(obj);
    }
}
