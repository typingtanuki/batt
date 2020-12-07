package com.github.typingtanuki.batt.utils;

public final class Progress {
    public static final String BATTERY_MATCH = "●";
    public static final String BATTERY_NO_MATCH = "○";
    public static final String BATTERY_BAD_PAGE = "❢";
    public static final String PAGE_DOWNLOAD = "⇣";
    public static final String PAGE_CACHED = "";
    public static final String CACHE_TIMEOUT = "T";
    public static final String IN_DB = "";
    public static final String NOT_IN_DB = "_";

    private static int progress = 0;

    private Progress() {
        super();
    }

    public static void progressStart(String s) {
        if (progress % 100 != 0) {
            System.out.println("\r\n");
        }
        progress = 0;

        progress(s + ": ");
    }

    public static void progress(String s) {
        for (char c : s.toCharArray()) {
            System.out.print(c);
            if (++progress % 100 == 0) {
                System.out.println();
            }
        }
    }
}
