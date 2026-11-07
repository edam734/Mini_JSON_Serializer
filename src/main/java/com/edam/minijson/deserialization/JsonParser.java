package com.edam.minijson.deserialization;

import com.edam.minijson.exception.JsonDeserializationException;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonParser {
    private static final List<String> numbers = List.of("0", "1", "2", "3", "4", "5", "6", "7", "8", "9");
    private static final List<Character> escapedChars = List.of('"', '\\', 'b', 'f', 'n', 'r', 't');

    private final String json;
    private int index;

    public JsonParser(String json) {
        this.json = json;
        this.index = 0;
    }

    public Object parse() {
        if (json.isEmpty()) {
            throw new JsonDeserializationException("Json can't be empty");
        }
        Object result = parseValue();
        index = skipWhitespace();

        if (index != json.length()) {
            throw new JsonDeserializationException("Unexpected characters after JSON");
        }

        return result;
    }

    public Object parseValue() {
        index = skipWhitespace();

        if (index >= json.length()) {
            throw new JsonDeserializationException("Missing JSON value");
        }
        String s = json.substring(index, index + 1);

        switch (s) {
            case "n" -> {
                return parseNull();
            }
            case "{" -> {
                return parseObject();
            }
            case "[" -> {
                return parseArray();
            }
            case "\"" -> {
                return parseString();
            }
            case "t" -> {
                return parseBoolean("true");
            }
            case "f" -> {
                return parseBoolean("false");
            }
        }

        if (numbers.contains(s) || s.equals("-")) {
            return parseNumber();
        }

        throw new JsonDeserializationException("Invalid JSON value");
    }

    private Object parseNumber() {
        StringBuilder sb = new StringBuilder();
        while (index < json.length()) {
            char c = json.charAt(index);
            if (c == ',' || c == ']' || c == '}' || c == ' ' || c == '\t' || c == '\n' || c == '\r')
                break;
            sb.append(c);
            index++;
        }

        String num = sb.toString();

        String regex = "-?(0|[1-9]\\d*)(\\.\\d+)?([eE][+-]?\\d+)?";
        if (!num.matches(regex)) {
            throw new JsonDeserializationException("Invalid number: " + num);
        }

        if (num.contains(".") || num.contains("e") || num.contains("E")) {
            return new BigDecimal(num);
        } else {
            return new BigInteger(num);
        }
    }

    private Object parseNull() {
        if (json.startsWith("null", index)) {
            index += 4;
            return null;
        }
        throw new JsonDeserializationException("invalid JSON");
    }

    private Object parseBoolean(String bool) {
        if (json.startsWith(bool, index)) {
            index += bool.length();
            return Boolean.parseBoolean(bool);
        }
        throw new JsonDeserializationException("Invalid JSON");
    }

    private String parseString() {
        if (index >= json.length() || json.charAt(index) != '"')
            throw new JsonDeserializationException("Expected string");

        index++;

        StringBuilder sb = new StringBuilder();
        char c;
        while (index < json.length() && (c = json.charAt(index)) != '"') {
            if (c == '\\') {
                index++;
                if (index >= json.length())
                    throw new JsonDeserializationException("Invalid escape");

                c = json.charAt(index);
                if (c == '/') {
                    sb.append('/');
                    index++;
                } else if (escapedChars.contains(c)) {
                    String escapeLiteral = "\\" + c;
                    sb.append(escapeLiteral.translateEscapes());
                    index++;
                } else if (c == 'u') {
                    if (index + 5 > json.length())
                        throw new JsonDeserializationException("Invalid Unicode");

                    String u = json.substring(index + 1, index + 5);
                    try {
                        sb.append((char) Integer.parseInt(u, 16));
                    } catch (NumberFormatException e) {
                        throw new JsonDeserializationException("Invalid Unicode");
                    }
                    index += 5;
                } else {
                    throw new JsonDeserializationException("Invalid escape");
                }
            } else {
                if (c < 0x20) throw new JsonDeserializationException("Invalid character");

                sb.append(c);
                index++;
            }
        }

        if (index >= json.length()) throw new JsonDeserializationException("Unclosed string");

        index++;
        return sb.toString();
    }

    private Map<String, Object> parseObject() {
        Map<String, Object> map = new HashMap<>();

        index++;
        while (index < json.length()) {
            index = skipWhitespace();

            if (index >= json.length()) break;
            if (json.charAt(index) == '}') {
                index++;
                return map;
            }

            String key = parseString();
            index = skipWhitespace();

            if (index >= json.length() || json.charAt(index) != ':')
                throw new JsonDeserializationException("Expected ':'");

            index++;
            Object value = parseValue();
            map.put(key, value);

            index = skipWhitespace();
            if (index >= json.length()) break;

            char c = json.charAt(index);
            if (c == ',') {
                index++;
                index = skipWhitespace();
                if (index >= json.length() || json.charAt(index) == '}')
                    throw new JsonDeserializationException("Invalid JSON");
            } else if (c == '}') {
                index++;
                return map;
            } else {
                throw new JsonDeserializationException("Invalid JSON");
            }
        }

        throw new JsonDeserializationException("Unclosed JSON object");
    }

    private List<Object> parseArray() {
        List<Object> list = new ArrayList<>();

        index++;
        while (index < json.length()) {
            index = skipWhitespace();

            if (index >= json.length()) break;
            if (json.charAt(index) == ']') {
                index++;
                return list;
            }

            Object element = parseValue();
            list.add(element);

            index = skipWhitespace();
            if (index >= json.length()) break;

            char c = json.charAt(index);
            if (c == ']') {
                index++;
                return list;
            } else if (c == ',') {
                index++;
                index = skipWhitespace();
                if (index >= json.length() || json.charAt(index) == ']')
                    throw new JsonDeserializationException("Invalid JSON array");
            } else {
                throw new JsonDeserializationException("Invalid JSON array");
            }
        }

        throw new JsonDeserializationException("Unclosed JSON array");
    }

    private int skipWhitespace() {
        while (index < json.length()) {
            char c = json.charAt(index);

            if (c != ' ' && c != '\t' && c != '\n' && c != '\r') {
                break;
            }
            index++;
        }

        return index;
    }
}