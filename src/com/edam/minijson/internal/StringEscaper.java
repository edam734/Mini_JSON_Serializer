package com.edam.minijson.internal;

public class StringEscaper {

    /**
     * Escapes special characters in a string according to the JSON specification (RFC 8259). Double
     * quotes and backslashes are escaped with a preceding backslash, while control characters
     * (U+0000 to U+001F) are represented using Unicode escape sequences (\u005cuXXXX).
     *
     * <p><b>Precondition:</b> {@code text != null}</p>
     *
     * @param text the string to escape
     * @return the escaped string, without surrounding double quotes
     */
    public static String escape(String text) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '"' || c == '\\') {
                sb.append("\\").append(c);
            } else if (c >= 0x00 && c <= 0x1F) {
                sb.append(String.format("\\u%04x", (int) c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}