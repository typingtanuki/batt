package com.github.typingtanuki.batt.validator;

import com.github.typingtanuki.batt.battery.Battery;

public interface Validator {
    boolean isValid(Battery battery);
}
