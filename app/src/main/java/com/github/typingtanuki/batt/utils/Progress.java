package com.github.typingtanuki.batt.utils;

public final class Progress {
    private static int progress = 0;

    private Progress() {
        super();
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
