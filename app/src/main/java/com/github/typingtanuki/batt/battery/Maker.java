package com.github.typingtanuki.batt.battery;

public class Maker {
    private final String name;
    private final String url;

    public Maker(String name, String url) {
        super();

        this.name = name;
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }
}
