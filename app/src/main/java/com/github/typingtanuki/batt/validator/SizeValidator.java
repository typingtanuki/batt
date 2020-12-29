package com.github.typingtanuki.batt.validator;

import com.github.typingtanuki.batt.battery.Battery;

public class SizeValidator implements Validator {
    private final boolean optional;
    private final double width;
    private final double height;
    private final double thickness;

    public SizeValidator(boolean optional, double width, double height, double thickness) {
        super();

        this.optional = optional;

        this.width = width;
        this.height = height;
        this.thickness = thickness;
    }

    @Override
    public boolean isValid(Battery battery) {
        if (optional && battery.getSize().isBlank()) {
            return true;
        }
        double[] sizes = battery.getSizes();
        if (sizes[0] > width) {
            return false;
        }
        if (sizes[1] > height) {
            return false;
        }
        if (sizes[2] > thickness) {
            return false;
        }
        return true;
    }
}
