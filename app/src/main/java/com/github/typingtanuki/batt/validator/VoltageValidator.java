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
        Double volt = battery.getVolt();
        if (volt == null) {
            return true;
        }
        return volt >= minVoltage && volt <= maxVoltage;
    }
}
