package com.github.typingtanuki.batt.validator;

import com.github.typingtanuki.batt.battery.Battery;

public class Condition {
    private final Validator[] validators;
    private final String name;

    public Condition(String name, Validator... validators) {
        super();
        this.name = name;
        this.validators = validators;
    }

    public boolean isValid(Battery battery) {
        for (Validator validator : validators) {
            if (!validator.isValid(battery)) {
                return false;
            }
        }
        return true;
    }

    public String getName() {
        return name;
    }
}
