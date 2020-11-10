package com.github.typingtanuki.batt.utils;

public final class Progress {
    private static int progress = 0;

    private Progress() {
        super();
    }

    public static void progress(String s) {
        System.out.print(s);
        if (++progress % 100 == 0) {
            System.out.println();
        }
    }
}
