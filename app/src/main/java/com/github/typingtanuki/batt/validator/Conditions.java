package com.github.typingtanuki.batt.validator;

import com.github.typingtanuki.batt.battery.Battery;
import com.github.typingtanuki.batt.battery.BatteryConnector;
import com.github.typingtanuki.batt.battery.BatteryForm;
import com.github.typingtanuki.batt.battery.BatteryType;

import java.util.Set;

public final class Conditions {
    private static final Condition[] CONDITIONS = new Condition[]{
            new Condition("panel_mate",
                    new VoltageValidator(7.4, 8.2),
                    new AmperageValidator(3_500, 1_000_000),
                    new ConnectorValidator(false,
                            BatteryConnector.PANDA_PM,
                            BatteryConnector.PM,
                            BatteryConnector.MAYBE_PM),
                    new TypeValidator(true,
                            BatteryType.LI_POLYMER),
                    new FormValidator(true,
                            BatteryForm.SQUARE,
                            BatteryForm.RECTANGLE)),
            new Condition("pin10",
                    new VoltageValidator(7.4, 8.2),
                    new AmperageValidator(3_500, 1_000_000),
                    new ConnectorValidator(true,
                            BatteryConnector.PIN_10,
                            BatteryConnector.PANDA_PM,
                            BatteryConnector.PM,
                            BatteryConnector.MAYBE_PM),
                    new TypeValidator(true,
                            BatteryType.LI_POLYMER),
                    new FormValidator(true,
                            BatteryForm.SQUARE,
                            BatteryForm.RECTANGLE))
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
