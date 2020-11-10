package com.github.typingtanuki.batt.scrapper;

import com.github.typingtanuki.batt.battery.Battery;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class CommonScrap {
    private static final Pattern MODEL_EXTRACT = Pattern.compile(".*Model:\\s(.+)$");
    private static final Pattern DESCRIPTION_EXTRACT = Pattern.compile("(.*),\\s[^,]+\\s([0-9]+)\\scell[^,]+$");
    private static final Pattern VOLT_EXTRACT = Pattern.compile(".*(?:\\s|or)([0-9.]+)V.*");
    private static final Pattern AMP_EXTRACT = Pattern.compile(".*\\s([0-9.]+)mAh.*");
    private static final Pattern WATT_EXTRACT = Pattern.compile(".*(?:\\s|\\()([0-9.]+)Wh.*");

    private CommonScrap() {
        super();
    }

    public static void readVolt(String s, Battery battery) {
        Matcher voltMatcher = VOLT_EXTRACT.matcher(s);
        if (voltMatcher.matches()) {
            battery.setVolt(Double.parseDouble(voltMatcher.group(1)));
            return;
        }
        throw new IllegalStateException("Could not extract volt from " + s);
    }

    public static void readAmp(String s, Battery battery) {
        Matcher ampMatcher = AMP_EXTRACT.matcher(s);
        if (ampMatcher.matches()) {
            battery.setAmp(Integer.parseInt(ampMatcher.group(1)));
        }
        // Amps are optional
    }

    public static void readWatt(String s, Battery battery) {
        Matcher wattMatcher = WATT_EXTRACT.matcher(s);
        if (wattMatcher.matches()) {
            battery.setWatt(Double.parseDouble(wattMatcher.group(1)));
        }
        // Watts are optional
    }

    public static void readDescription(String s, Battery battery) {
        Matcher descriptionMatcher = DESCRIPTION_EXTRACT.matcher(s);
        if (descriptionMatcher.matches()) {
            battery.setDescription(descriptionMatcher.group(1).strip());
            battery.setCells(Integer.parseInt(descriptionMatcher.group(2)));
        } else {
            battery.setDescription(s.strip());
        }
    }

    public static void readModel(String s, Battery battery) {
        Matcher modelMatcher = MODEL_EXTRACT.matcher(s);
        if (modelMatcher.matches()) {
            battery.setModel(modelMatcher.group(1).strip());
        }
    }
}
