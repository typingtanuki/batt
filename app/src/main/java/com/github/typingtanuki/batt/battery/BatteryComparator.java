package com.github.typingtanuki.batt.battery;

import com.github.typingtanuki.batt.exceptions.NoPartException;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings("SE_COMPARATOR_SHOULD_BE_SERIALIZABLE")
public class BatteryComparator implements java.util.Comparator<Battery> {
    @Override
    public int compare(Battery o1, Battery o2) {
        int connector = Integer.compare(o1.getConnector().ordinal(), o2.getConnector().ordinal());
        if (connector != 0) {
            return connector;
        }

        int amp = Integer.compare(o2.getAmp(), o1.getAmp());
        if (amp != 0) {
            return amp;
        }

        int watt = Double.compare(o2.getWatt(), o1.getWatt());
        if (watt != 0) {
            return watt;
        }
        try {
            return o1.getModel().compareTo(o2.getModel());
        } catch (NoPartException e) {
            return -10;
        }
    }
}
