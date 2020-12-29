package com.github.typingtanuki.batt.output;

import com.github.typingtanuki.batt.battery.Battery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ForumOutput {
    private static final String LIST_START = "[list]";
    private static final String LIST_END = "[/list]";
    private static final String LIST_BULLET = "[*] ";
    private static final String EOL = "\r\n";

    private final List<Battery> batteries;

    public ForumOutput(List<Battery> batteries) {
        super();
        this.batteries = batteries;
    }

    public String generate() {
        StringBuilder output = new StringBuilder();
        output.append(LIST_START).append(EOL);
        for (Battery battery : batteries) {
            output.append(entry(battery)).append(EOL);
        }
        output.append(LIST_END).append(EOL);
        return output.toString();
    }

    private String entry(Battery battery) {
        battery.consolidate();
        return LIST_BULLET + battery.getMaker().getName() + " " + battery.getModel() + EOL +
                LIST_START + EOL +
                LIST_BULLET + EOL +
                LIST_BULLET + format1(battery.getVolt()) + "V" + EOL +
                LIST_BULLET + "Up to: " + battery.getAmp() + "mAh" + EOL +
                LIST_BULLET + "Parts: " + formatParts(battery) + EOL +
                LIST_BULLET + "Size: " + formatSize(battery) + EOL +
                LIST_END + EOL;
    }

    private static String formatSize(Battery battery) {
        if (battery.getSize().isBlank()) {
            return "";
        }
        double[] sizes = battery.getSizes();
        return sizes[0] + "mm x " + sizes[1] + "mm x " + sizes[2] + "mm";
    }

    private static String formatParts(Battery battery) {
        List<String> parts = new ArrayList<>(battery.getPartNo());
        Collections.sort(parts);
        return String.join(", ", parts);
    }

    private static String format1(Double value) {
        return String.format("%.1f", value);
    }
}
