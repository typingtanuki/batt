package com.github.typingtanuki.batt.battery;

public enum BatteryType {
    LI_POLYMER,
    LI_ION,
    UNKNOWN;

    public static BatteryType parse(String type) {
        String value = type
                .replaceAll("LITHIUM", "LI")
                .replaceAll("ION POLYMER", "POLYMER");
        return BatteryType.valueOf(value);
    }
}
