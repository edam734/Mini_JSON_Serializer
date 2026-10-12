# Mini JSON Serializer

<br>

A lightweight JSON serialization library built entirely with **Java SE**, inspired by Jackson.

The goal of this project is to develop a simple, generic, and reusable serializer capable of converting Java objects into valid JSON strings using **Reflection** and **recursion**, without relying on external libraries.

The library will eventually be packaged as a standalone JAR, allowing it to be integrated into other Java applications.

<br>

## Features

The following features are planned:

- **Strings:** Proper escaping of special characters according to RFC 8259.
- **Primitives and Wrappers:** Support for numbers, booleans, and characters.
- **Null Values:** Correct JSON representation of `null`.
- **Arrays:** Serialization of primitive, object, and multidimensional arrays.
- **Collections:** Support for Java collections, including lists and sets.
- **Maps:** Conversion of maps into JSON objects, with appropriate key handling.
- **Java Objects:** Automatic field serialization using Reflection.
- **Nested Objects:** Recursive serialization of complex object structures.
- **Circular References:** Detection and handling to prevent infinite recursion.
- **Error Handling:** Meaningful exceptions for unsupported types and serialization failures.

<br>

## Example Usage

The library is intended to expose a simple public API:

```java
String json = JsonSerializer.toJson(object);
```

For example, given a Java object representing a user, the serializer should produce:

```json
{
  "name": "John",
  "age": 30,
  "active": true
}
```

*Note: This example illustrates the intended functionality. The library is currently under development.*

<br>

## Architecture

The initial architecture consists of a small set of components with clearly defined responsibilities:

| Component | Responsibility |
|---|---|
| `JsonSerializer` | Public API and entry point for serialization. |
| `SerializationEngine` | Core serialization logic and type handling. |
| `StringEscaper` | Escaping special characters in JSON strings. |
| `SerializationContext` | Tracking object references during serialization. |
| `JsonSerializationException` | Reporting serialization errors. |

The architecture is intentionally minimal and may evolve as development progresses.

<br>

## Development Roadmap

- [ ] **Phase 1 — Basic JSON Values:** Strings, numbers, booleans, characters, null values, and character escaping.
- [ ] **Phase 2 — Arrays:** Primitive arrays, object arrays, and multidimensional arrays.
- [ ] **Phase 3 — Collections:** Lists, sets, and other Java collections.
- [ ] **Phase 4 — Maps:** Map serialization and key validation.
- [ ] **Phase 5 — Reflection:** Automatic serialization of Java object fields.
- [ ] **Phase 6 — Nested Objects and Circular References:** Recursive serialization and cycle detection.
- [ ] **Phase 7 — Testing and Packaging:** Unit tests, documentation, and standalone JAR distribution.

<br>

## Technical Requirements

- Java SE
- No external JSON libraries or frameworks
- Generic serialization independent of application-specific classes
- Reflection-based object inspection
- Recursive handling of nested data structures
- Modular, maintainable code with clear responsibilities

<br>

## Out of Scope

The initial version will not include:

- JSON deserialization
- Custom annotations
- Advanced serialization configuration
- Full compatibility with Jackson or Gson

<br>

## Integration

The first application intended to use this library is **Mini HTTP Server**, a separate Java SE project.

The serializer will be imported as a JAR dependency and used to generate JSON response bodies for HTTP requests.

The library is designed to remain independent of HTTP-specific functionality and reusable in other Java applications.

<br>

## Project Status

**Under development.**

This project is primarily intended as a practical exercise in Java SE, Reflection, recursion, API design, and reusable library development.

<br>
