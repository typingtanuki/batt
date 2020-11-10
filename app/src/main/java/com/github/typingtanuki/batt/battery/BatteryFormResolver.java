package com.github.typingtanuki.batt.battery;

import java.util.LinkedHashMap;
import java.util.Map;

public final class BatteryFormResolver {
    private static final Map<String, BatteryForm> FORM_FACTOR = new LinkedHashMap<>();

    static {
        FORM_FACTOR.put("1003522-100434-2", BatteryForm.CUSTOM);
        FORM_FACTOR.put("1003565-100434-2", BatteryForm.CUSTOM);
        FORM_FACTOR.put("1003565-100434-1", BatteryForm.CUSTOM);
        FORM_FACTOR.put("1003350-100102-1", BatteryForm.CUSTOM);
        FORM_FACTOR.put("1003350-100102-2", BatteryForm.CUSTOM);
        FORM_FACTOR.put("1005480-100564-1", BatteryForm.CUSTOM);
        FORM_FACTOR.put("1005480-100564-2", BatteryForm.CUSTOM);
        FORM_FACTOR.put("1004937-100200-1", BatteryForm.CUSTOM);
        FORM_FACTOR.put("1004937-100200-2", BatteryForm.CUSTOM);
        FORM_FACTOR.put("1005450-100444-1", BatteryForm.FAT);
        FORM_FACTOR.put("1004118-100443-2", BatteryForm.RECTANGLE);
        FORM_FACTOR.put("1003452-100108-2", BatteryForm.RECTANGLE);
        FORM_FACTOR.put("1003471-100113-1", BatteryForm.RECTANGLE);
        FORM_FACTOR.put("1003471-100113-2", BatteryForm.RECTANGLE);
        FORM_FACTOR.put("1003798-100113-1", BatteryForm.RECTANGLE);
        FORM_FACTOR.put("1003252-100113-1", BatteryForm.RECTANGLE);
        FORM_FACTOR.put("1003252-100113-2", BatteryForm.RECTANGLE);
        FORM_FACTOR.put("1003571-100113-1", BatteryForm.RECTANGLE);
        FORM_FACTOR.put("1003571-100113-2", BatteryForm.RECTANGLE);
        FORM_FACTOR.put("1003843-100399-1", BatteryForm.RECTANGLE);
        FORM_FACTOR.put("1003843-100399-2", BatteryForm.RECTANGLE);
        FORM_FACTOR.put("1004118-100399-1", BatteryForm.RECTANGLE);
        FORM_FACTOR.put("1004049-100430-1", BatteryForm.RECTANGLE);
        FORM_FACTOR.put("1004049-100430-2", BatteryForm.RECTANGLE);
        FORM_FACTOR.put("1004399-100401-2", BatteryForm.RECTANGLE);
        FORM_FACTOR.put("1004397-100154-1", BatteryForm.RECTANGLE);
        FORM_FACTOR.put("1004397-100154-2", BatteryForm.RECTANGLE);
        FORM_FACTOR.put("1003972-100430-1", BatteryForm.SQUARE);
        FORM_FACTOR.put("1004399-100449-1", BatteryForm.SQUARE);
        FORM_FACTOR.put("1004970-100441-1", BatteryForm.SQUARE);
        FORM_FACTOR.put("1004970-100441-2", BatteryForm.SQUARE);
        FORM_FACTOR.put("1005351-100592-2", BatteryForm.SQUARE);

        FORM_FACTOR.put("1004894-100105-1", BatteryForm.SQUARE);
        FORM_FACTOR.put("1004894-100105-2", BatteryForm.SQUARE);
        FORM_FACTOR.put("1004970-100517-1", BatteryForm.SQUARE);
        FORM_FACTOR.put("1004970-100517-2", BatteryForm.SQUARE);
    }

    private BatteryFormResolver() {
        super();
    }

    public static void resolveForm(Battery battery) {
        if (FORM_FACTOR.containsKey(battery.getModel())) {
            battery.setForm(FORM_FACTOR.get(battery.getModel()));
        }
    }
}
