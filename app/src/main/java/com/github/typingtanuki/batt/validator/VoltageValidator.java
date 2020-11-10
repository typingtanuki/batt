package com.github.typingtanuki.batt.validator;

import com.github.typingtanuki.batt.battery.Battery;

public class VoltageValidator implements Validator {
    private final double minVoltage;
    private final double maxVoltage;

    public VoltageValidator(double minVoltage, double maxVoltage) {
        this.minVoltage = minVoltage;
        this.maxVoltage = maxVoltage;
    }

    @Override
    public boolean isValid(Battery battery) {
        if (battery.getVolt() < minVoltage) {
            return false;
        }
        if (battery.getVolt() > maxVoltage) {
            return false;
        }
        return true;
    }
}
