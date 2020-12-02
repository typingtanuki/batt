package com.github.typingtanuki.batt.battery;

import java.util.Locale;

public class Maker {
    private final String name;
    private final String url;

    public Maker(String name, String url) {
        super();

        this.name = name;
        this.url = url;
    }

    public String getName() {
        return name.substring(0, 1).toUpperCase(Locale.ENGLISH) +
                name.substring(1).toLowerCase(Locale.ENGLISH);
    }

    public String getUrl() {
        return url;
    }
}
