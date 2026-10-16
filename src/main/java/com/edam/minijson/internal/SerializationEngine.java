package com.edam.minijson.internal;

import com.edam.minijson.exception.JsonSerializationException;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.*;
import java.time.temporal.Temporal;
import java.util.*;
import java.util.stream.Collectors;

public class SerializationEngine {

    public String serialize(Object obj) {
        SerializationContext context = new SerializationContext();
        return serialize(context, obj);
    }

    public String serialize(SerializationContext context, Object obj) {
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
        } else if (obj instanceof UUID) {
            return "\"" + obj + "\"";
        } else if (obj instanceof Number) {
            if (isNaN(obj)) {
                return "\"NaN\"";
            }
            if (isInfinity(obj)) {
                return "\"" + obj + "\"";
            }
            return String.valueOf(obj);
        } else if (obj instanceof Optional<?> optional) {
            if (optional.isEmpty()) {
                return "null";
            }
            return serialize(optional.get());
        } else if (obj instanceof OptionalInt optionalInt) {
            if (optionalInt.isEmpty()) {
                return "null";
            }
            return serialize(optionalInt.getAsInt());
        } else if (obj instanceof OptionalLong optionalLong) {
            if (optionalLong.isEmpty()) {
                return "null";
            }
            return serialize(optionalLong.getAsLong());
        } else if (obj instanceof OptionalDouble optionalDouble) {
            if (optionalDouble.isEmpty()) {
                return "null";
            }
            return serialize(optionalDouble.getAsDouble());
        } else if (Collection.class.isAssignableFrom(fieldClass)) {
            if (!context.addObject(obj)) {
                return null;
            }
            try {
                return "[" + ((Collection<?>) obj).stream()
                        .map(elem -> serialize(context, elem))
                        .filter(Objects::nonNull)
                        .collect(Collectors.joining(",")) + "]";
            } finally {
                context.removeObject(obj);
            }
        } else if (Map.class.isAssignableFrom(fieldClass)) {
            if (!context.addObject(obj)) {
                return null;
            }
            try {
                Set<? extends Map.Entry<?, ?>> entries = ((Map<?, ?>) obj).entrySet();
                return "{" + entries.stream().map(entry -> {
                    String serialized = serialize(context, entry.getValue());
                    if (serialized != null) {
                        return serialize(context, String.valueOf(entry.getKey())) + ":" +
                                serialized;
                    }
                    return null;
                }).filter(Objects::nonNull).collect(Collectors.joining(",")) + "}";
            } finally {
                context.removeObject(obj);
            }
        } else if (fieldClass.isArray()) {
            if (!context.addObject(obj)) {
                return null;
            }

            StringBuilder sb = new StringBuilder();
            int length = Array.getLength(obj);

            boolean first = true;
            try {
                for (int i = 0; i < length; i++) {
                    Object element = Array.get(obj, i);
                    String serialized = serialize(context, element);
                    if (serialized != null) {
                        if (!first) {
                            sb.append(",");
                        }
                        first = false;
                        sb.append(serialized);
                    }
                }
            } finally {
                context.removeObject(obj);
            }
            return "[" + sb + "]";
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

            if (isJdkModule(fieldClass.getModule())) {
                throw new JsonSerializationException(
                        "Unsupported Java type: " + fieldClass.getName());
            }

            // is custom object
            if (!context.addObject(obj)) {
                return null;
            }

            StringBuilder sb = new StringBuilder("{");
            boolean first = true;

            List<Field> fields = getAllFields(fieldClass);

            for (Field field : fields) {
                int modifiers = field.getModifiers();

                if (Modifier.isStatic(modifiers) || Modifier.isTransient(modifiers) ||
                        field.isSynthetic()) {
                    continue;
                }

                if (!field.canAccess(obj) && !field.trySetAccessible()) {
                    throw new JsonSerializationException("Cannot access field: " + field);
                }

                Object value;
                try {
                    value = field.get(obj);
                } catch (IllegalAccessException e) {
                    throw new JsonSerializationException("Cannot access field: " + field, e);
                }

                try {
                    String serialized = serialize(context, value);
                    if (serialized != null) {
                        if (!first) {
                            sb.append(",");
                        }
                        sb.append("\"")
                                .append(StringEscaper.escape(field.getName()))
                                .append("\":")
                                .append(serialized);
                    }
                } finally {
                    context.removeObject(value);
                }

                first = false;
            }

            return sb.append("}").toString();
        }
    }

    private boolean isNaN(Object obj) {
        return (obj instanceof Double d && d.isNaN()) || obj instanceof Float f && f.isNaN();
    }

    private boolean isInfinity(Object obj) {
        return (obj instanceof Double d && d.isInfinite()) ||
                obj instanceof Float f && f.isInfinite();
    }

    private boolean isKnownTemporal(Object obj) {
        return obj instanceof LocalDate || obj instanceof LocalTime ||
                obj instanceof LocalDateTime || obj instanceof Instant ||
                obj instanceof OffsetDateTime || obj instanceof ZonedDateTime;
    }

    private boolean isJdkModule(Module classModule) {
        String moduleName = classModule.getName();
        return moduleName != null &&
                (moduleName.startsWith("java.") || moduleName.startsWith("jdk."));
    }

    private List<Field> getAllFields(Class<?> clazz) {
        return getAllFields(new ArrayList<>(), new HashSet<>(), clazz);
    }

    private List<Field> getAllFields(List<Field> result, Set<String> foundFields, Class<?> clazz) {
        if (Object.class != clazz) {
            for (Field field : clazz.getDeclaredFields()) {
                int modifiers = field.getModifiers();
                if (!Modifier.isStatic(modifiers) && !Modifier.isTransient(modifiers) &&
                        !field.isSynthetic()) {
                    if (foundFields.add(field.getName())) {
                        result.add(field);
                    }
                }
            }
            return getAllFields(result, foundFields, clazz.getSuperclass());
        } else {
            return result;
        }
    }
}
