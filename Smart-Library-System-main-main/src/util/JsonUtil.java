package util;

import Model.*;
import java.util.*;

/**
 * Zero-dependency lightweight JSON serializer and parser for REST APIs.
 * Supports Maps, Lists, Numbers, Booleans, Strings, and Models with toMap().
 */
public class JsonUtil {

    public static String toJson(Object obj) {
        if (obj == null) {
            return "null";
        }
        if (obj instanceof String) {
            return escapeString((String) obj);
        }
        if (obj instanceof Number || obj instanceof Boolean) {
            return obj.toString();
        }
        if (obj instanceof Book) {
            return toJson(((Book) obj).toMap());
        }
        if (obj instanceof User) {
            return toJson(((User) obj).toSafeMap());
        }
        if (obj instanceof Transaction) {
            return toJson(((Transaction) obj).toMap());
        }
        if (obj instanceof Reservation) {
            return toJson(((Reservation) obj).toMap());
        }
        if (obj instanceof Library) {
            return toJson(((Library) obj).toMap());
        }
        if (obj instanceof Map<?, ?>) {
            Map<?, ?> map = (Map<?, ?>) obj;
            StringBuilder sb = new StringBuilder("{");
            boolean first = true;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (!first) sb.append(",");
                first = false;
                sb.append(escapeString(String.valueOf(entry.getKey())));
                sb.append(":");
                sb.append(toJson(entry.getValue()));
            }
            sb.append("}");
            return sb.toString();
        }
        if (obj instanceof Collection<?>) {
            Collection<?> col = (Collection<?>) obj;
            StringBuilder sb = new StringBuilder("[");
            boolean first = true;
            for (Object item : col) {
                if (!first) sb.append(",");
                first = false;
                sb.append(toJson(item));
            }
            sb.append("]");
            return sb.toString();
        }
        if (obj.getClass().isArray()) {
            Object[] arr = (Object[]) obj;
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < arr.length; i++) {
                if (i > 0) sb.append(",");
                sb.append(toJson(arr[i]));
            }
            sb.append("]");
            return sb.toString();
        }
        return escapeString(obj.toString());
    }

    private static String escapeString(String s) {
        if (s == null) return "\"\"";
        StringBuilder sb = new StringBuilder("\"");
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\b': sb.append("\\b"); break;
                case '\f': sb.append("\\f"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < 32) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        sb.append("\"");
        return sb.toString();
    }

    /**
     * Parses a JSON string into a Java Map<String, Object>.
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> parseObject(String json) {
        if (json == null) return Collections.emptyMap();
        Object parsed = new JsonParser(json.trim()).parse();
        if (parsed instanceof Map<?, ?>) {
            return (Map<String, Object>) parsed;
        }
        return Collections.emptyMap();
    }

    private static class JsonParser {
        private final String src;
        private int idx = 0;

        public JsonParser(String src) {
            this.src = src;
        }

        public Object parse() {
            skipWhitespace();
            if (idx >= src.length()) return null;
            char c = src.charAt(idx);
            if (c == '{') return parseMap();
            if (c == '[') return parseList();
            if (c == '"') return parseString();
            if (c == 't' || c == 'f') return parseBoolean();
            if (c == 'n') return parseNull();
            if (Character.isDigit(c) || c == '-') return parseNumber();
            return null;
        }

        private void skipWhitespace() {
            while (idx < src.length() && Character.isWhitespace(src.charAt(idx))) {
                idx++;
            }
        }

        private Map<String, Object> parseMap() {
            Map<String, Object> map = new LinkedHashMap<>();
            idx++; // skip '{'
            skipWhitespace();
            if (idx < src.length() && src.charAt(idx) == '}') {
                idx++;
                return map;
            }
            while (idx < src.length()) {
                skipWhitespace();
                String key = parseString();
                skipWhitespace();
                if (idx < src.length() && src.charAt(idx) == ':') {
                    idx++;
                }
                skipWhitespace();
                Object val = parse();
                map.put(key, val);
                skipWhitespace();
                if (idx < src.length() && src.charAt(idx) == ',') {
                    idx++;
                } else if (idx < src.length() && src.charAt(idx) == '}') {
                    idx++;
                    break;
                }
            }
            return map;
        }

        private List<Object> parseList() {
            List<Object> list = new ArrayList<>();
            idx++; // skip '['
            skipWhitespace();
            if (idx < src.length() && src.charAt(idx) == ']') {
                idx++;
                return list;
            }
            while (idx < src.length()) {
                skipWhitespace();
                Object val = parse();
                list.add(val);
                skipWhitespace();
                if (idx < src.length() && src.charAt(idx) == ',') {
                    idx++;
                } else if (idx < src.length() && src.charAt(idx) == ']') {
                    idx++;
                    break;
                }
            }
            return list;
        }

        private String parseString() {
            if (idx >= src.length() || src.charAt(idx) != '"') return "";
            idx++; // skip opening quote
            StringBuilder sb = new StringBuilder();
            while (idx < src.length()) {
                char c = src.charAt(idx++);
                if (c == '"') break;
                if (c == '\\' && idx < src.length()) {
                    char esc = src.charAt(idx++);
                    switch (esc) {
                        case '"': sb.append('"'); break;
                        case '\\': sb.append('\\'); break;
                        case '/': sb.append('/'); break;
                        case 'b': sb.append('\b'); break;
                        case 'f': sb.append('\f'); break;
                        case 'n': sb.append('\n'); break;
                        case 'r': sb.append('\r'); break;
                        case 't': sb.append('\t'); break;
                        case 'u':
                            if (idx + 4 <= src.length()) {
                                String hex = src.substring(idx, idx + 4);
                                sb.append((char) Integer.parseInt(hex, 16));
                                idx += 4;
                            }
                            break;
                        default: sb.append(esc);
                    }
                } else {
                    sb.append(c);
                }
            }
            return sb.toString();
        }

        private Boolean parseBoolean() {
            if (src.startsWith("true", idx)) {
                idx += 4;
                return true;
            } else if (src.startsWith("false", idx)) {
                idx += 5;
                return false;
            }
            return null;
        }

        private Object parseNull() {
            if (src.startsWith("null", idx)) {
                idx += 4;
            }
            return null;
        }

        private Number parseNumber() {
            int start = idx;
            if (idx < src.length() && (src.charAt(idx) == '-' || src.charAt(idx) == '+')) {
                idx++;
            }
            while (idx < src.length() && (Character.isDigit(src.charAt(idx)) || src.charAt(idx) == '.' || src.charAt(idx) == 'e' || src.charAt(idx) == 'E')) {
                idx++;
            }
            String numStr = src.substring(start, idx);
            if (numStr.contains(".")) {
                return Double.parseDouble(numStr);
            }
            try {
                return Integer.parseInt(numStr);
            } catch (NumberFormatException e) {
                return Long.parseLong(numStr);
            }
        }
    }
}
