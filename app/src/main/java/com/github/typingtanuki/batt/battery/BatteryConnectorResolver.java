package com.github.typingtanuki.batt.battery;

import java.util.LinkedHashMap;
import java.util.Map;

public final class BatteryConnectorResolver {
    private static final Map<String, BatteryConnector> CONNECTORS = new LinkedHashMap<>();

    private BatteryConnectorResolver() {
        super();
    }

    public static void resolveConnector(Battery battery) {
        if (CONNECTORS.containsKey(battery.getModel())) {
            battery.setConnector(CONNECTORS.get(battery.getModel()));
        }
    }
}
