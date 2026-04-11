package hlquery.utils;

import java.util.HashMap;
import java.util.Map;

public class Config {
    public static Map<String, Object> mergeDefaults(Map<String, Object> options) {
        Map<String, Object> defaults = new HashMap<>();
        defaults.put("base_url", "http://localhost:9200");
        defaults.put("timeout", 30);
        defaults.put("auth_method", "bearer");

        if (options == null) {
            return defaults;
        }

        Map<String, Object> merged = new HashMap<>(defaults);
        merged.putAll(options);
        return merged;
    }

    public static String normalizeUrl(String url) {
        if (url == null) return null;
        url = url.trim();
        if (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        return url;
    }

    public static boolean isValidUrl(String url) {
        if (url == null) return false;
        return url.startsWith("http://") || url.startsWith("https://");
    }
}
