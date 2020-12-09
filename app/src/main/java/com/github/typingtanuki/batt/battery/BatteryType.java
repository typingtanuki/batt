package com.github.typingtanuki.batt.battery;

public enum BatteryType {
    LI_POLYMER,
    LI_ION,
    UNKNOWN;

    public static BatteryType parse(String type) {
        String value = type
                .replaceAll("LITHIUM", "LI")
                .replaceAll("ION POLYMER", "POLYMER")
                .replaceAll("PANASONIC", "UNKNOWN")
                .replaceAll("SAMSUNG", "UNKNOWN")
                .replaceAll("タイプ", "");
        value = value.strip();
        value = value.replaceAll(" ", "_");
        if (value.contains("UNKNOWN")) {
            return BatteryType.UNKNOWN;
        }
        return BatteryType.valueOf(value);
    }
}
