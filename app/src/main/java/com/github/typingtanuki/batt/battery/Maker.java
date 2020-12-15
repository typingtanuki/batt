package com.github.typingtanuki.batt.battery;

import java.util.Locale;

public class Maker {
    private final String name;
    private final Source source;

    public Maker(String name, Source source) {
        super();

        String clean = name
                .replaceAll("\\s", " ")
                .replaceAll("[^A-Za-z0-9\\- ]", "")
                .strip();

        if (clean.isBlank()) {
            this.name = "Other";
        } else {
            this.name = clean;
        }
        this.source = source.compact();
    }

    public String getName() {
        return name.substring(0, 1).toUpperCase(Locale.ENGLISH) +
                name.substring(1).toLowerCase(Locale.ENGLISH);
    }

    public Source getSource() {
        return source;
    }
}
