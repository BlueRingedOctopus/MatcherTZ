package tz.java;

import org.junit.Test;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.fail;

public class MatcherTest {

    @Test
    public void testValidRegexAndText() {
        assertTrue("Expected regex to match text", Matcher.matches("\\d+", "123"));
    }

    @Test
    public void testInvalidRegexAndText() {
        assertFalse("Expected regex not to match text", Matcher.matches("\\d+", "abc"));
    }

    @Test(expected = Matcher.InvalidInputException.class)
    public void testNullRegexThrowsException() {
        Matcher.matches(null, "123");
    }

    @Test(expected = Matcher.InvalidInputException.class)
    public void testNullTextThrowsException() {
        Matcher.matches("\\d+", null);
    }

    @Test
    public void testCacheCleanup() throws InterruptedException {
        // Добавляем записи в кэш
        for (int i = 0; i < 105; i++) {
            Matcher.matches("pattern" + i, "text" + i);
        }

        // Проверяем, что кэш ограничен
        assertTrue("Cache should not exceed max size", Matcher.cache.size() <= 100);

        // Ждём истечения времени для старых записей
        Thread.sleep(61000);

        // Добавляем ещё одну запись и проверяем, что устаревшие записи удалены
        Matcher.matches("newPattern", "newText");
        assertTrue("Cache should clean up old entries", Matcher.cache.size() <= 100);
    }
}
