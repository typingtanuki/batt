package com.github.typingtanuki.batt.validator;

import com.github.typingtanuki.batt.battery.Battery;
import com.github.typingtanuki.batt.battery.BatteryConnector;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;

public class ConnectorValidator implements Validator {
    private final boolean optional;
    private final Set<BatteryConnector> connectors;

    public ConnectorValidator(boolean optional, BatteryConnector... connectors) {
        this.optional = optional;
        this.connectors = EnumSet.noneOf(BatteryConnector.class);
        this.connectors.addAll(Arrays.asList(connectors));
    }

    @Override
    public boolean isValid(Battery battery) {
        if (optional && battery.getConnector() == null) {
            return true;
        }
        if (optional && battery.getConnector() == BatteryConnector.UNKNOWN) {
            return true;
        }
        return connectors.contains(battery.getConnector());
    }
}
