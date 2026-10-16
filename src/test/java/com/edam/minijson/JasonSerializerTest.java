package com.edam.minijson;

import com.edam.minijson.exception.JsonSerializationException;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

    private static class Person {
        private String name;
        private Person friend;

        public Person(String name) {
            this.name = name;
        }
    }

    private static class PersonHolder {
        private Person first;
        private Person second;

        public PersonHolder(Person first, Person second) {
            this.first = first;
            this.second = second;
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
    void shouldSerializeUUID() {
        UUID uuid = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

        assertEquals("\"550e8400-e29b-41d4-a716-446655440000\"", JsonSerializer.toJson(uuid));
    }

    @Test
    void shouldSerializeNaN() {
        assertEquals("\"NaN\"", JsonSerializer.toJson(Double.NaN));
    }

    @Test
    void shouldSerializeInfinity() {
        assertEquals("\"Infinity\"", JsonSerializer.toJson(Double.POSITIVE_INFINITY));
        assertEquals("\"-Infinity\"", JsonSerializer.toJson(Double.NEGATIVE_INFINITY));
    }

    @Test
    void shouldSerializeOptional() {
        record User(String name) {
        }

        assertEquals("{\"name\":\"John\"}", JsonSerializer.toJson(Optional.of(new User("John"))));

        assertEquals("null", JsonSerializer.toJson(Optional.empty()));
    }

    @Test
    void shouldSerializeOptionalInt() {
        assertEquals("25", JsonSerializer.toJson(OptionalInt.of(25)));

        assertEquals("null", JsonSerializer.toJson(OptionalInt.empty()));
    }

    @Test
    void shouldSerializeOptionalLong() {
        assertEquals("123456789", JsonSerializer.toJson(OptionalLong.of(123456789L)));

        assertEquals("null", JsonSerializer.toJson(OptionalLong.empty()));
    }

    @Test
    void shouldSerializeOptionalDouble() {
        assertEquals("12.5", JsonSerializer.toJson(OptionalDouble.of(12.5)));

        assertEquals("null", JsonSerializer.toJson(OptionalDouble.empty()));
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
    void shouldRejectUnsupportedJdkTypes() {
        JsonSerializationException exception = assertThrows(JsonSerializationException.class,
                () -> JsonSerializer.toJson(new Thread()));

        assertEquals("Unsupported Java type: java.lang.Thread", exception.getMessage());
    }

    @Test
    void shouldSerializeObject() {
        User user = new User(1, "John", "john@gmail.com");

        String expected = "{\"id\":1,\"name\":\"John\",\"email\":\"john@gmail.com\"}";

        assertEquals(expected, JsonSerializer.toJson(user));
    }

    @Test
    void shouldSerializeInheritedFieldsWithoutDuplicates() {
        class Grandparent {
            String name = "Grandparent";
            int age = 70;
            String country = "Portugal";
        }

        class Parent extends Grandparent {
            String name = "Parent";
            int age = 45;
            String profession = "Engineer";
        }

        class Child extends Parent {
            String name = "Child";
            int age = 20;
            String hobby = "Gaming";

            static String country = "Spain";
            transient String profession = "Doctor";
        }

        String json = JsonSerializer.toJson(new Child());

        assertEquals(
                "{\"name\":\"Child\",\"age\":20,\"hobby\":\"Gaming\",\"profession\":\"Engineer\",\"country\":\"Portugal\"}",
                json);
    }

    @Test
    void shouldHandleCircularReferences() {
        Person a = new Person("John");
        Person b = new Person("Mary");

        a.friend = b;
        b.friend = a;

        String expected = "{\"name\":\"John\",\"friend\":{\"name\":\"Mary\"}}";

        assertEquals(expected, JsonSerializer.toJson(a));
    }

    @Test
    void shouldSerializeRepeatedReferences() {
        Person mary = new Person("Mary");

        PersonHolder holder = new PersonHolder(mary, mary);

        String expected = "{\"first\":{\"name\":\"Mary\",\"friend\":null}," +
                "\"second\":{\"name\":\"Mary\",\"friend\":null}}";

        assertEquals(expected, JsonSerializer.toJson(holder));
    }

    @Test
    void shouldHandleRepeatedAndCircularReferences() {
        Person mary = new Person("Mary");
        Person bob = new Person("Bob");

        mary.friend = bob;
        bob.friend = mary;

        PersonHolder holder = new PersonHolder(mary, mary);

        String expected = "{" + "\"first\":{\"name\":\"Mary\",\"friend\":{\"name\":\"Bob\"}}," +
                "\"second\":{\"name\":\"Mary\",\"friend\":{\"name\":\"Bob\"}}" + "}";

        assertEquals(expected, JsonSerializer.toJson(holder));
    }

    @Test
    void shouldHandleCircularCollection() {
        List<Object> list = new ArrayList<>();

        list.add("John");
        list.add(list); // Circular reference
        list.add("Mary");

        String expected = "[\"John\",\"Mary\"]";

        assertEquals(expected, JsonSerializer.toJson(list));
    }

    @Test
    void shouldHandleCircularMap() {
        Map<String, Object> map = new LinkedHashMap<>();

        map.put("name", "John");
        map.put("self", map); // Circular reference
        map.put("age", 30);

        String expected = "{\"name\":\"John\",\"age\":30}";

        assertEquals(expected, JsonSerializer.toJson(map));
    }

    @Test
    void shouldHandleCircularArray() {
        Object[] array = new Object[3];

        array[0] = "John";
        array[1] = array; // Circular reference
        array[2] = "Mary";

        String expected = "[\"John\",\"Mary\"]";

        assertEquals(expected, JsonSerializer.toJson(array));
    }

    @Test
    void shouldHandleCircularObjectInsideCollection() {
        Person mary = new Person("Mary");
        Person bob = new Person("Bob");
        Person john = new Person("John");

        mary.friend = bob;
        bob.friend = mary;

        List<Person> people = new ArrayList<>();
        people.add(mary);
        people.add(john);

        String expected = "[" + "{\"name\":\"Mary\",\"friend\":{\"name\":\"Bob\"}}," +
                "{\"name\":\"John\",\"friend\":null}" + "]";

        assertEquals(expected, JsonSerializer.toJson(people));
    }

    @Test
    void shouldHandleMultipleCircularReferencesInArray() {
        Object[] array = new Object[4];

        array[0] = array;
        array[1] = array;
        array[2] = "John";
        array[3] = "Mary";

        String expected = "[\"John\",\"Mary\"]";

        assertEquals(expected, JsonSerializer.toJson(array));
    }

    @Test
    void shouldSerializeComplexNestedStructure() {
        Person mary = new Person("Mary");
        Person bob = new Person("Bob");

        mary.friend = bob;
        bob.friend = mary; // Circular reference

        List<Object> people = new ArrayList<>();
        people.add(mary);
        people.add(mary); // Repeated reference
        people.add(null);
        people.add(new int[]{1, 2, 3});

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("people", people);
        map.put("status", "active");
        map.put("self", map); // Circular reference

        Object[] root = new Object[5];
        root[0] = map;
        root[1] = new Object[]{true, "Hello", mary};
        root[2] = map; // Repeated reference
        root[3] = root; // Circular reference
        root[4] = null;

        String expectedPerson = "{\"name\":\"Mary\",\"friend\":{\"name\":\"Bob\"}}";

        String expectedMap =
                "{\"people\":[" + expectedPerson + "," + expectedPerson + "," + "null,[1,2,3]" +
                        "],\"status\":\"active\"}";

        String expected =
                "[" + expectedMap + "," + "[true,\"Hello\"," + expectedPerson + "]," + expectedMap +
                        "," + "null" + "]";

        assertEquals(expected, JsonSerializer.toJson(root));
    }
}
