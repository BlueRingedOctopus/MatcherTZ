package tz.java;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class Matcher {
    public static Map<String, Pattern> cache = new HashMap<String, Pattern>();

    public static boolean matches(String regex, String text) {
        if (regex == null || text == null) {
            throw new NullPointerException();
        }
        Pattern pattern = cache.computeIfAbsent(regex, Pattern::compile);
        return pattern.matcher(text).matches();
    }
}
