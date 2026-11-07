package com.edam.minijson.deserialization;

import com.edam.minijson.exception.JsonDeserializationException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JsonDeserializerTest {

    @Test
    void shouldRejectUnexpectedCharactersAfterJson() {
        String json = "123abc";

        assertThrows(JsonDeserializationException.class,
                () -> new JsonParser(json).parse());
    }

    @Test
    void shouldParseSimpleObject() {
        String json = """
                {
                    "name": "John",
                    "age": 25
                }
                """;

        Map<?, ?> result = (Map<?, ?>) new JsonParser(json).parse();

        assertEquals(2, result.size());
        assertEquals("John", result.get("name"));
        assertEquals(25, ((Number) result.get("age")).intValue());
    }

    @Test
    void shouldParseSimpleArray() {
        String json = """
                ["John", "Mary", "Peter"]
                """;

        List<?> result = (List<?>) new JsonParser(json).parse();

        assertEquals(List.of("John", "Mary", "Peter"), result);
    }

    @Test
    void shouldParseArrayOfObjects() {
        String json = """
                [
                    {"name": "John", "age": 25},
                    {"name": "Mary", "age": 30}
                ]
                """;

        List<?> result = (List<?>) new JsonParser(json).parse();

        assertEquals(2, result.size());

        Map<?, ?> john = (Map<?, ?>) result.get(0);
        Map<?, ?> mary = (Map<?, ?>) result.get(1);

        assertEquals("John", john.get("name"));
        assertEquals(25, ((Number) john.get("age")).intValue());

        assertEquals("Mary", mary.get("name"));
        assertEquals(30, ((Number) mary.get("age")).intValue());
    }

    @Test
    void shouldParseNestedArrays() {
        String json = """
                [
                    ["John", "Mary"],
                    ["Peter", "Anna"],
                    ["Bruce", ["James", "Sarah"]]
                ]
                """;

        List<?> result = (List<?>) new JsonParser(json).parse();

        assertEquals(3, result.size());
        assertEquals(List.of("John", "Mary"), result.get(0));
        assertEquals(List.of("Peter", "Anna"), result.get(1));
        assertEquals(List.of("Bruce", List.of("James", "Sarah")), result.get(2));
    }

    @Test
    void shouldParseEmptyArray() {
        String json = "[]";

        List<?> result = (List<?>) new JsonParser(json).parse();

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldParseMixedArray() {
        String json = """
                [
                    "John",
                    25,
                    true,
                    null,
                    {"city": "Braga"},
                    [10, 20]
                ]
                """;

        List<?> result = (List<?>) new JsonParser(json).parse();

        assertEquals(6, result.size());
        assertEquals("John", result.get(0));
        assertEquals(25, ((Number) result.get(1)).intValue());
        assertEquals(true, result.get(2));
        assertNull(result.get(3));
        assertEquals("Braga", ((Map<?, ?>) result.get(4)).get("city"));

        List<?> numbers = (List<?>) result.get(5);
        assertEquals(10, ((Number) numbers.get(0)).intValue());
        assertEquals(20, ((Number) numbers.get(1)).intValue());
    }

    @Test
    void shouldRejectInvalidArrays() {
        assertThrows(JsonDeserializationException.class, () -> new JsonParser("[1, 2,]").parse());

        assertThrows(JsonDeserializationException.class, () -> new JsonParser("[1 2]").parse());

        assertThrows(JsonDeserializationException.class, () -> new JsonParser("[1, 2").parse());
    }
}