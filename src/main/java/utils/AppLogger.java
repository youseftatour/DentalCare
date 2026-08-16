package utils;

import org.slf4j.LoggerFactory;

public final class AppLogger {
    private AppLogger() {
    }

    public static void error(Class<?> source, String message, Throwable error) {
        LoggerFactory.getLogger(source).error(message, error);
    }

    public static void warn(Class<?> source, String message, Object value) {
        LoggerFactory.getLogger(source).warn(message, value);
    }

    public static void warn(Class<?> source, String message, Throwable error) {
        LoggerFactory.getLogger(source).warn(message, error);
    }
}
