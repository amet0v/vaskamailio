package com.nurtel.vaskamailio.db.config;

public class DatabaseContextHolder {
    private static final ThreadLocal<String> CONTEXT = new ThreadLocal<>();

    public static void set(String dbKey) {
        CONTEXT.set(dbKey);
    }

    public static String get() {
        return CONTEXT.get();
    }

    public static void clear() {
        CONTEXT.remove();
    }
}