package com.github.typingtanuki.batt.battery;

public class BatteryComparator implements java.util.Comparator<Battery> {
    @Override
    public int compare(Battery o1, Battery o2) {
        int amp = Integer.compare(o1.getAmp(), o2.getAmp());
        if (amp != 0) {
            return -amp;
        }

        int cell = Integer.compare(o1.getCells(), o2.getCells());
        if (cell != 0) {
            return -cell;
        }
        int watt = Double.compare(o1.getWatt(), o2.getWatt());
        if (watt != 0) {
            return -watt;
        }
        return o1.getModel().compareTo(o2.getModel());
    }
}
