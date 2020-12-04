package com.github.typingtanuki.batt.battery;

import java.util.Locale;

public class Maker {
    private final String name;
    private final Source source;

    public Maker(String name, Source source) {
        super();

        if (name.isBlank()) {
            this.name = "None";
        } else {
            this.name = name;
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
