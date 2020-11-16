package com.github.typingtanuki.batt.battery;

public class MakerComparator implements java.util.Comparator<Maker> {
    @Override
    public int compare(Maker o1, Maker o2) {
        return o1.getName().compareTo(o2.getName());
    }
}
