package com.github.typingtanuki.batt.validator;

import com.github.typingtanuki.batt.battery.Battery;

import java.util.Arrays;
import java.util.List;

public class AndValidator implements Validator {
    private final List<Validator> validators;

    public AndValidator(Validator... validators) {
        this.validators = Arrays.asList(validators);
    }

    @Override
    public boolean isValid(Battery battery) {
        for (Validator validator : validators) {
            if (!validator.isValid(battery)) {
                return false;
            }
        }
        return true;
    }
}
