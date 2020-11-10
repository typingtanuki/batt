package com.github.typingtanuki.batt.validator;

import com.github.typingtanuki.batt.battery.Battery;
import com.github.typingtanuki.batt.battery.BatteryForm;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;

public class FormValidator implements Validator {
    private final boolean optional;
    private final Set<BatteryForm> forms;

    public FormValidator(boolean optional, BatteryForm... forms) {
        this.optional = optional;
        this.forms = EnumSet.noneOf(BatteryForm.class);
        this.forms.addAll(Arrays.asList(forms));
    }

    @Override
    public boolean isValid(Battery battery) {
        if (optional && battery.getForm() == null) {
            return true;
        }
        if (optional && battery.getForm() == BatteryForm.UNKNOWN) {
            return true;
        }
        return forms.contains(battery.getForm());
    }
}
