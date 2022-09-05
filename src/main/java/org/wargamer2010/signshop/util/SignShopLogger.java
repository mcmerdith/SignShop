package org.wargamer2010.signshop.util;

import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.configuration.SignShopConfig;

import java.util.logging.Level;

public class SignShopLogger {
    public static SignShopLogger STORAGE;
    public static SignShopLogger DATABASE;

    public static SignShopLogger getStorageLogger() {
        if (STORAGE == null) STORAGE = new SignShopLogger("Storage");
        return STORAGE;
    }

    public static SignShopLogger getDatabaseLogger() {
        if (DATABASE == null) DATABASE = new SignShopLogger("Database");
        return DATABASE;
    }

    private String name;

    private SignShopLogger() {
    }

    /**
     * Create a logger with `name` as the prefix
     *
     * @param name The name of the logger
     */
    public SignShopLogger(String name) {
        this.name = name;
    }

    /**
     * Log an error
     *
     * @param error The error
     */
    public void error(String error) {
        exception(null, error);
    }

    /**
     * Log a non-fatal exception
     *
     * @param e The exception
     */
    public void exception(Exception e) {
        exception(e, null);
    }

    /**
     * Log a non-fatal exception
     *
     * @param e     The exception
     * @param cause A more detailed cause for the error
     */
    @SuppressWarnings("ThrowableNotThrown") // method with return is only used for convenience
    public void exception(Exception e, String cause) {
        exception(e, cause, false);
    }

    /**
     * Log an exception
     *
     * @param e     The exception
     * @param fatal If the exception is fatal
     * @return If fatal, an {@link IllegalStateException} with <code>exception</code> as the cause
     */
    public RuntimeException exception(Exception e, boolean fatal) {
        return exception(e, null, fatal);
    }

    /**
     * Log an exception
     *
     * @param exception The exception. If provided, {@link Exception#getCause()} is recursively searched and logged
     * @param fatal     If the exception is fatal
     * @param cause     A more detailed cause for the error
     * @return If fatal, a {@link RuntimeException} with <code>cause</code> as the message (or <code>{@link Exception#getMessage()}</code> if null), or null if not fatal
     */
    public RuntimeException exception(Exception exception, String cause, boolean fatal) {
        String nonNullException = (exception == null) ? "" : exception.getClass().getSimpleName() + ": " + exception.getMessage();

        String mainMessage = (cause == null) ? nonNullException : cause;
        String detailMessage = (cause == null || exception == null) ? "" : nonNullException;

        Level level = (fatal) ? Level.SEVERE : Level.WARNING;

        log(String.format("(%s) %s%s",
                level.getName(),
                mainMessage,
                detailMessage.trim().isEmpty() ? "" : String.format(" (%s)", detailMessage)
        ), level);

        // Log only the message of the causes of the exception
        // If we throw the exception normally, the stacktrace is super long and unreadable
        Throwable currentCause = (exception == null) ? null : exception.getCause();
        while (currentCause != null) {
            log("   Caused by " + currentCause.getClass().getSimpleName() + ": " + currentCause.getMessage(), level);
            currentCause = currentCause.getCause();
        }

        // If we're throwing the exception, provide a wrapped exception without the causes (we just logged them, don't do it twice)
        return (fatal) ? new RuntimeException(mainMessage) : null;
    }

    /**
     * Log a message at {@link Level#INFO}
     *
     * @param message The message to log
     */
    public void info(String message) {
        log(message, Level.INFO);
    }

    /**
     * Log a message
     *
     * @param message The message to log
     * @param level   The log level
     */
    public void log(String message, Level level) {
        SignShop.log(((name != null && !name.trim().isEmpty()) ? String.format("[%s] ", name) : "") + message, level);
    }

    public void debug(String message) {
        if (SignShopConfig.debugging()) log("[debug] " + message, Level.INFO);
    }
}
