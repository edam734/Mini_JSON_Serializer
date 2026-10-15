package com.edam.minijson;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JasonSerializerTest {

    enum Status {
        LOGGED_IN, LOGGED_OFF
    }

    private static class User {
        private final int id;
        private final String name;
        private final String email;

        public User(int id, String name, String email) {
            this.id = id;
            this.name = name;
            this.email = email;
        }
    }

    @Test
    void shouldSerializeNull() {
        assertEquals("null", com.edam.minijson.JsonSerializer.toJson(null));
    }

    @Test
    void shouldSerializeBoolean() {
        assertEquals("true", JsonSerializer.toJson(true));
    }

    @Test
    void shouldSerializeString() {
        assertEquals("\"Hello\"", JsonSerializer.toJson("Hello"));
    }

    @Test
    void shouldSerializeEnum() {
        assertEquals("\"LOGGED_IN\"", JsonSerializer.toJson(Status.LOGGED_IN));
        assertEquals("\"LOGGED_OFF\"", JsonSerializer.toJson(Status.LOGGED_OFF));
    }

    @Test
    void shouldSerializeArray() {
        int[] numbers = {1, 2, 3};

        assertEquals("[1,2,3]", JsonSerializer.toJson(numbers));
    }

    @Test
    void shouldSerializeCollection() {
        List<User> users = new ArrayList<>();

        users.add(new User(1, "John", "john@gmail.com"));
        users.add(new User(2, "Mary", "mary@gmail.com"));
        users.add(new User(3, "Peter", "peter@gmail.com"));

        String expected = "[" + "{\"id\":1,\"name\":\"John\",\"email\":\"john@gmail.com\"}," +
                "{\"id\":2,\"name\":\"Mary\",\"email\":\"mary@gmail.com\"}," +
                "{\"id\":3,\"name\":\"Peter\",\"email\":\"peter@gmail.com\"}" + "]";

        assertEquals(expected, JsonSerializer.toJson(users));
    }

    @Test
    void shouldSerializeMap() {
        Map<String, User> users = new LinkedHashMap<>();

        users.put("admin", new User(1, "John", "john@gmail.com"));
        users.put("guest", new User(2, "Mary", "mary@gmail.com"));

        String expected =
                "{" + "\"admin\":{\"id\":1,\"name\":\"John\",\"email\":\"john@gmail.com\"}," +
                        "\"guest\":{\"id\":2,\"name\":\"Mary\",\"email\":\"mary@gmail.com\"}" + "}";

        assertEquals(expected, JsonSerializer.toJson(users));
    }

    @Test
    void shouldSerializeObject() {
        User user = new User(1, "John", "john@gmail.com");

        String expected = "{\"id\":1,\"name\":\"John\",\"email\":\"john@gmail.com\"}";

        assertEquals(expected, JsonSerializer.toJson(user));
    }
}
