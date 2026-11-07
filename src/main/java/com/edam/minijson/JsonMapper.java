package com.edam.minijson;

import com.edam.minijson.deserialization.JsonParser;
import com.edam.minijson.exception.JsonSerializationException;
import com.edam.minijson.serialization.SerializationEngine;

/**
 * Provides methods for converting Java objects to and from JSON.
 *
 * <p>Supports primitive values, strings, arrays, collections, maps,
 * and custom Java objects.
 *
 * @author edam
 */
public class JsonMapper {

    /**
     * Serializes a Java object into its JSON representation.
     *
     * @param obj the object to serialize; may be null
     * @return a JSON string representing the object
     * @throws JsonSerializationException if the object cannot be serialized
     */
    public static String toJson(Object obj) {
        SerializationEngine serializationEngine = new SerializationEngine();
        return serializationEngine.serialize(obj);
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        JsonParser jsonParser = new JsonParser(json);
        jsonParser.parse();
        return null;
    }
}
