package util;

public final class BackendLogger {
    private BackendLogger() {
    }

    public static synchronized void info(String message) {
        log("INFO", message);
    }

    public static synchronized void warn(String message) {
        log("WARN", message);
    }

    public static synchronized void error(String message) {
        log("ERROR", message);
    }

    private static void log(String level, String message) {
        System.out.println(message);
    }
}