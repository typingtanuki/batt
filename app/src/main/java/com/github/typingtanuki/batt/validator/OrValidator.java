package com.github.typingtanuki.batt.validator;

import com.github.typingtanuki.batt.battery.Battery;

import java.util.Arrays;
import java.util.List;

public class OrValidator implements Validator {
    private final List<Validator> validators;

    public OrValidator(Validator... validators) {
        this.validators = Arrays.asList(validators);
    }

    @Override
    public boolean isValid(Battery battery) {
        for (Validator validator : validators) {
            if (validator.isValid(battery)) {
                return true;
            }
        }
        return false;
    }
}
