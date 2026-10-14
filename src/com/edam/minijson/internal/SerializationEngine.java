package com.edam.minijson.internal;

import com.edam.minijson.exception.JsonSerializationException;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.*;
import java.time.temporal.Temporal;
import java.util.*;

public class SerializationEngine {

    public String serialize(Object obj) throws IllegalAccessException {
        if (obj == null) {
            return "null";
        }

        Class<?> fieldClass = obj.getClass();

        if (Object.class == fieldClass) {
            return "{}";
        } else if (obj instanceof String || obj instanceof Character) {
            return "\"" + StringEscaper.escape(String.valueOf(obj)) + "\"";
        } else if (obj instanceof Boolean) {
            return Boolean.toString((boolean) obj);
        } else if (obj instanceof Number) {
            return String.valueOf(obj);
        } else if (Collection.class.isAssignableFrom(fieldClass)) {
            return ""; // TODO
        } else if (Map.class.isAssignableFrom(fieldClass)) {
            return ""; // TODO
        } else if (fieldClass.isArray()) {
            return ""; // TODO
        } else if (obj instanceof Enum<?>) {
            return "\"" + ((Enum<?>) obj).name() + "\"";
        } else if (obj instanceof java.util.Date) {
            if (obj instanceof java.sql.Date || obj instanceof java.sql.Time) {
                return "\"" + obj + "\"";
            } else {
                return "\"" + ((java.util.Date) obj).toInstant().toString() + "\"";
            }
        } else if (isKnownTemporal(obj)) {
            return "\"" + obj + "\"";
        } else if (Temporal.class.isAssignableFrom(fieldClass)) {
            throw new JsonSerializationException(
                    "Unsupported temporal type: " + obj.getClass().getName());
        } else {
            // is custom object
            StringBuilder sb = new StringBuilder();

            boolean started = false;
            List<Field> fields = getAllFields(new ArrayList<>(), fieldClass);
            for (Field field : fields) {
                int modifiers = field.getModifiers();
                if (!Modifier.isStatic(modifiers) && !Modifier.isTransient(modifiers) &&
                        !field.isSynthetic()) {
                    if (started) {
                        sb.append(",");
                    }
                    sb.append("\"").append(field.getName()).append("\":");
                    if (!field.canAccess(modifiers)) {
                        field.setAccessible(true);
                    }
                    String serialized = serialize(field.get(obj));
                    sb.append(serialized);

                    started = true;
                }
            }
            return "{" + sb + "}";
        }
    }

    private boolean isKnownTemporal(Object obj) {
        return obj instanceof LocalDate || obj instanceof LocalTime ||
                obj instanceof LocalDateTime || obj instanceof Instant ||
                obj instanceof OffsetDateTime || obj instanceof ZonedDateTime;
    }

    private List<Field> getAllFields(List<Field> result, Class<?> clazz) {
        if (Object.class != clazz) {
            Collections.addAll(result, clazz.getDeclaredFields());
            return getAllFields(result, clazz.getSuperclass());
        } else {
            return result;
        }
    }

    static void main() throws IllegalAccessException {

        //        Object o = "Isto é uma \"String\".";
        Object u = new User(1, "John", "johnny@gmail.com", Status.LOGGED_OFF);
        Date date = new Date(50000);
        Temporal t = Instant.now();
        SerializationEngine engine = new SerializationEngine();
        String json = engine.serialize(t);
        System.out.println(json);
    }

    /**********************
     *  For some testing  *
     **********************/

    enum Status {
        LOGGED_IN, LOGGED_OFF;
    }

    static class User {
        private int id;
        private String name;
        private String email;
        private SerializationEngine.Status status;

        public User(int id, String name, String email, SerializationEngine.Status status) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.status = status;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getEmail() {
            return email;
        }

        public SerializationEngine.Status getStauts() {
            return status;
        }
    }

}
