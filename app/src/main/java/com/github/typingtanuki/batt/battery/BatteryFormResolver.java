package com.github.typingtanuki.batt.battery;

import java.util.LinkedHashMap;
import java.util.Map;

public final class BatteryFormResolver {
    private static final Map<String, BatteryForm> FORM_FACTOR = new LinkedHashMap<>();

    private BatteryFormResolver() {
        super();
    }

    public static void resolveForm(Battery battery) {
        if (FORM_FACTOR.containsKey(battery.getModel())) {
            battery.setForm(FORM_FACTOR.get(battery.getModel()));
        }
    }
}
