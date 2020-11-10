package com.github.typingtanuki.batt.validator;

import com.github.typingtanuki.batt.battery.Battery;

public class AmperageValidator implements Validator {
    private final int minAmperage;
    private final int maxAmperage;

    public AmperageValidator(int minAmperage, int maxAmperage) {
        this.minAmperage = minAmperage;
        this.maxAmperage = maxAmperage;
    }

    @Override
    public boolean isValid(Battery battery) {
        if (battery.getAmp() < minAmperage) {
            return false;
        }
        if (battery.getAmp() > maxAmperage) {
            return false;
        }
        return true;
    }
}
