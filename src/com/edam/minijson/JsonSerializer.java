package com.edam.minijson;

import com.edam.minijson.internal.SerializationEngine;

public class JsonSerializer {

    private final SerializationEngine serializationEngine;

    public JsonSerializer(SerializationEngine serializationEngine) {
        this.serializationEngine = serializationEngine;
    }

    public String toJson(Object obj) {
        return serializationEngine.serialize(obj);
    }
}
