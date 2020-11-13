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
        Integer amp = battery.getAmp();
        if (amp == null) {
            return true;
        }
        return amp >= minAmperage && amp <= maxAmperage;
    }
}
