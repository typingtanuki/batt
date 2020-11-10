package com.github.typingtanuki.batt.battery;

import java.util.LinkedHashMap;
import java.util.Map;

public final class BatteryConnectorResolver {
    private static final Map<String, BatteryConnector> CONNECTORS = new LinkedHashMap<>();

    static {
        CONNECTORS.put("1003522-100434-2", BatteryConnector.CUSTOM);
        CONNECTORS.put("1003565-100434-2", BatteryConnector.CUSTOM);
        CONNECTORS.put("1003565-100434-1", BatteryConnector.CUSTOM);
        CONNECTORS.put("1003350-100102-1", BatteryConnector.CUSTOM);
        CONNECTORS.put("1003350-100102-2", BatteryConnector.CUSTOM);
        CONNECTORS.put("1004118-100443-2", BatteryConnector.CUSTOM);
        CONNECTORS.put("1004049-100430-1", BatteryConnector.CUSTOM);
        CONNECTORS.put("1004049-100430-2", BatteryConnector.CUSTOM);
        CONNECTORS.put("1003972-100430-1", BatteryConnector.CUSTOM);
        CONNECTORS.put("1004399-100449-1", BatteryConnector.CUSTOM);
        CONNECTORS.put("1004399-100401-2", BatteryConnector.CUSTOM);
        CONNECTORS.put("1004970-100441-1", BatteryConnector.CUSTOM);
        CONNECTORS.put("1004970-100441-2", BatteryConnector.CUSTOM);
        CONNECTORS.put("1003843-100399-1", BatteryConnector.PANELMATE);
    }

    private BatteryConnectorResolver() {
        super();
    }

    public static void resolveConnector(Battery battery) {
        if (CONNECTORS.containsKey(battery.getModel())) {
            battery.setConnector(CONNECTORS.get(battery.getModel()));
        }
    }
}
