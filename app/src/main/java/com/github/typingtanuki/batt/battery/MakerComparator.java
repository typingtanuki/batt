package com.github.typingtanuki.batt.battery;

public class MakerComparator implements java.util.Comparator<Maker> {
    @Override
    public int compare(Maker o1, Maker o2) {
        if (o1.getName().equalsIgnoreCase(o2.getName())) {
            return 0;
        }
        if ("denchipro".equalsIgnoreCase(o1.getName())) {
            return 10;
        }
        if ("denchipro".equalsIgnoreCase(o2.getName())) {
            return -10;
        }
        return o1.getName().compareTo(o2.getName());
    }
}
