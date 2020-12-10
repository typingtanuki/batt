package com.github.typingtanuki.batt.scrapper;

import com.github.typingtanuki.batt.battery.Battery;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class CommonScrap {
    private static final Pattern VOLT_EXTRACT = Pattern.compile(".*(?:\\s|or|/)([0-9.]+)V.*");
    private static final Pattern NO_VOLT_EXTRACT = Pattern.compile(".*\\sV\\s.*");
    private static final Pattern AMP_EXTRACT = Pattern.compile(".*\\s([0-9.]+)mAh.*");
    private static final Pattern WATT_EXTRACT = Pattern.compile(".*(?:\\s|\\()([0-9.]+)Wh.*");
    private static final Pattern SIZE_EXTRACT = Pattern.compile(".*(\\d+\\.?\\d?\\s?x\\s?\\d+\\.?\\d?\\s?x\\s?\\d+\\.?\\d?\\s?mm).*");

    private CommonScrap() {
        super();
    }

    public static void readVolt(String s, Battery battery) {
        Matcher voltMatcher = VOLT_EXTRACT.matcher(s);
        if (voltMatcher.matches()) {
            battery.setVolt(Double.parseDouble(voltMatcher.group(1)));
            return;
        }
        Matcher noVoltMatcher = NO_VOLT_EXTRACT.matcher(s);
        if (noVoltMatcher.matches()) {
            battery.setVolt(-1d);
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

    public static void readSize(String s, Battery battery) {
        Matcher sizeMatcher = SIZE_EXTRACT.matcher(s);
        if (sizeMatcher.matches()) {
            battery.setSize(sizeMatcher.group(1));
        }
        // Size are optional
    }
}
