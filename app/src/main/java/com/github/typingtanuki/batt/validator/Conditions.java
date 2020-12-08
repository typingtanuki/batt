package com.github.typingtanuki.batt.validator;

import com.github.typingtanuki.batt.battery.Battery;
import com.github.typingtanuki.batt.battery.BatteryConnector;
import com.github.typingtanuki.batt.battery.BatteryForm;
import com.github.typingtanuki.batt.battery.BatteryType;

import java.util.List;
import java.util.Set;

public final class Conditions {
    private static final Condition[] CONDITIONS = new Condition[]{
            new Condition("panel_mate",
                    new VoltageValidator(7.4, 8.2),
                    new AmperageValidator(1_000, 1_000_000),
                    new ConnectorValidator(false, BatteryConnector.PANEL_MATE),
                    new TypeValidator(true, BatteryType.LI_POLYMER),
                    new FormValidator(true, BatteryForm.SQUARE, BatteryForm.RECTANGLE, BatteryForm.FAT)),
            new Condition("pin10",
                    new VoltageValidator(7.4, 8.2),
                    new AmperageValidator(1_000, 1_000_000),
                    new ConnectorValidator(true, BatteryConnector.PIN_10, BatteryConnector.PANEL_MATE),
                    new TypeValidator(true, BatteryType.LI_POLYMER),
                    new FormValidator(true, BatteryForm.SQUARE, BatteryForm.RECTANGLE, BatteryForm.FAT)),
            new Condition("pin4",
                    new VoltageValidator(11, 15),
                    new AmperageValidator(1_000, 1_000_000),
                    new ConnectorValidator(true, BatteryConnector.PIN_4),
                    new TypeValidator(true, BatteryType.LI_POLYMER),
                    new FormValidator(true, BatteryForm.SQUARE, BatteryForm.RECTANGLE, BatteryForm.FAT))
    };

    private Conditions() {
        super();
    }

    public static void validate(Battery battery, Set<String> matchedConditions) {
        for (Condition condition : CONDITIONS) {
            if (condition.isValid(battery)) {
                matchedConditions.add(condition.getName());
            }
        }
    }
}
