package com.github.typingtanuki.batt.battery;

import com.github.typingtanuki.batt.exceptions.PageUnavailableException;

import java.io.IOException;
import java.util.List;
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

    public List<Battery> listBatteries() throws IOException, PageUnavailableException {
        return source.getScrapper().listBatteries(this);
    }

    public Battery extractBatteryDetails(Battery battery) throws IOException {
        return source.getScrapper().extractBatteryDetails(battery);
    }
}
