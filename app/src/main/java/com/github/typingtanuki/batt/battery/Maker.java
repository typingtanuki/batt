package com.github.typingtanuki.batt.battery;

import com.github.typingtanuki.batt.exceptions.NoPartException;
import com.github.typingtanuki.batt.exceptions.PageUnavailableException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class Maker {
    private final MakerName name;
    private final List<Source> sources;

    public Maker(String name, Source source) {
        this(name, Collections.singletonList(source));
    }

    public Maker(String name, List<Source> sources) {
        super();

        String clean = name
                .replaceAll("\\s", " ")
                .replaceAll("[^A-Za-z0-9\\- ]", "")
                .strip()
                .toUpperCase(Locale.ENGLISH);

        if (clean.isBlank()) {
            this.name = MakerName.OTHER;
        } else {
            this.name = MakerName.parse(clean);
        }
        List<Source> compacted = new ArrayList<>(sources.size());
        for (Source s : sources) {
            compacted.add(s.compact());
        }
        this.sources = compacted;
    }

    public String getName() {
        String str = name.name();
        return str.substring(0, 1).toUpperCase(Locale.ENGLISH) +
                str.substring(1).toLowerCase(Locale.ENGLISH);
    }

    public List<Source> getSources() {
        return sources;
    }

    public List<Battery> listBatteries() throws IOException, PageUnavailableException {
        List<Battery> out = new ArrayList<>();
        for (Source source : sources) {
            out.addAll(source.getScrapper().listBatteries(this));
        }
        return out;
    }

    public Battery extractBatteryDetails(Battery battery) throws IOException, NoPartException {
        return sources.get(0).getScrapper().extractBatteryDetails(battery);
    }

    public MakerName getMakerName() {
        return name;
    }
}
