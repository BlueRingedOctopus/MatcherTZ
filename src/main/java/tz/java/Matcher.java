package tz.java;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * Класс {@code Matcher} нужен для проверки соответствия текста регулярному выражению
 * с использованием кэширования для повышения производительности при повторном использовании одинаковых шаблонов.
 * Первая проблема, на которую я обратил внимание - избыточность кода. Регулярное выражение создавалось каждый раз
 * заново при новом запросе. Это можно исправить кэшированием. Кэш очищается от избыточных и устаревших данных.
 * Также я добавил кастомное исключение для проверки входных данных.
 */
public class Matcher {
    private static final int MAX_CACHE_SIZE = 100;
    private static final long MAX_CACHE_AGE_MS = 60000;
    static final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

    private static class CacheEntry {
        Pattern pattern;
        Instant lastAccess;

        CacheEntry(Pattern pattern) {
            this.pattern = pattern;
            this.lastAccess = Instant.now();
        }

        void updateLastAccess() {
            this.lastAccess = Instant.now();
        }
    }

    /**
     * Кастомное исключение, выбрасываемое в случае некорректных входных параметров (например, нулевых значений)
     */
    public static class InvalidInputException extends RuntimeException {
        public InvalidInputException(String message) {
            super(message);
        }
    }

    /**
     * Метод проверяет, соответствует ли заданный текст указанному регулярному выражению
     * @param regex регулярное выражение, которому должен соответствовать текст
     * @param text  текст, который необходимо проверить
     * @return {@code true}, если текст соответствует регулярному выражению, а если нет {@code false}
     * @throws InvalidInputException если {@code regex} или {@code text} равны {@code null}
     */
    public static boolean matches(String regex, String text) throws InvalidInputException {
        if (regex == null || text == null) {
            throw new InvalidInputException("Regex or text cannot be null");
        }

        synchronized (cache) {
            CacheEntry entry = cache.computeIfAbsent(regex, r -> new CacheEntry(Pattern.compile(r)));
            entry.updateLastAccess();
            cleanupCache();
            return entry.pattern.matcher(text).matches();
        }
    }

    /**
     * Удаляет из кэша записи, которые превышают максимальный размер или устарели
     * Этот метод вызывается автоматически для поддержания ограничений на кэш
     */
    private static void cleanupCache() {
        if (cache.size() <= MAX_CACHE_SIZE) return;

        Instant now = Instant.now();
        cache.entrySet().removeIf(entry ->
                cache.size() > MAX_CACHE_SIZE ||
                        now.toEpochMilli() - entry.getValue().lastAccess.toEpochMilli() > MAX_CACHE_AGE_MS
        );
    }
}
