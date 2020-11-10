package com.github.typingtanuki.batt.validator;

import com.github.typingtanuki.batt.battery.Battery;
import com.github.typingtanuki.batt.battery.BatteryType;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;

public class TypeValidator implements Validator {
    private final boolean optional;
    private final Set<BatteryType> types;

    public TypeValidator(boolean optional, BatteryType... types) {
        this.optional = optional;
        this.types = EnumSet.noneOf(BatteryType.class);
        this.types.addAll(Arrays.asList(types));
    }

    @Override
    public boolean isValid(Battery battery) {
        if (optional && battery.getType() == null) {
            return true;
        }
        return types.contains(battery.getType());
    }
}
