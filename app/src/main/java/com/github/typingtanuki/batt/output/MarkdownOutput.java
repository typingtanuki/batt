package com.github.typingtanuki.batt.output;

import com.github.typingtanuki.batt.battery.Battery;
import com.github.typingtanuki.batt.battery.BatteryConnector;
import com.github.typingtanuki.batt.battery.Source;

import java.util.*;

public class MarkdownOutput {
    private final List<Battery> batteries;

    public MarkdownOutput(List<Battery> batteries) {
        super();
        this.batteries = batteries;
    }

    private static String tableHeader() {
        return "| Brand | Power | Size | Cell | Connector | Form factor | Part No. | URL |\r\n" +
                "| ----- | ----- | ---- | ---- | --------- | ----------- | -------- | --- |\r\n";
    }

    private static String tableFooter() {
        return "\r\n\r\n";
    }

    public String generate() {
        StringBuilder output = new StringBuilder();
        output.append("Found: ")
                .append(batteries.size())
                .append("\r\n\r\n")
                .append(tableHeader());
        BatteryConnector connector = null;
        for (Battery battery : batteries) {
            if (connector == null) {
                connector = battery.getConnector();
            }
            if (connector != battery.getConnector()) {
                output.append(tableFooter());
                output.append(tableHeader());
                connector = battery.getConnector();
            }
            output.append(asTable(battery)).append("\r\n");
        }
        return output.toString();
    }

    private String asTable(Battery battery) {
        battery.consolidate();
        return "| " + makeList(battery.getBrands()) +
                " | " + makeList(formatPower(battery)) +
                " | " + battery.getSize() +
                " | " + formatCells(battery) +
                " | " + formatEnum(battery.getConnector()) +
                " | " + formatEnum(battery.getForm()) +
                " | " + makeList(filterSet(battery.getPartNo())) +
                " | " + formatUrls(battery) +
                " |";
    }

    private String formatCells(Battery battery) {
        if (battery.getCells() > 0) {
            return "" + battery.getCells();
        }
        return "--";
    }

    private List<String> formatPower(Battery battery) {
        return Arrays.asList(
                format1(battery.getVolt()) + "V",
                battery.getAmp() + "mAh",
                format2(battery.getWatt()) + "W");
    }

    private String formatEnum(Enum<?> enumEntry) {
        String str = enumEntry.name();
        if (str.equals("UNKNOWN")) {
            return "?";
        }
        str = str.toLowerCase(Locale.ENGLISH).replaceAll("_", " ");
        str = str.substring(0, 1).toUpperCase(Locale.ENGLISH) + str.substring(1);
        return str;
    }

    private String makeList(Collection<String> list) {
        StringBuilder out = new StringBuilder();
        out.append("<ul>");
        for (String s : list) {
            out.append("<li>").append(s).append("</li>");
        }
        out.append("</ul>");
        return out.toString();
    }

    private String formatUrls(Battery battery) {
        StringBuilder out = new StringBuilder();
        out.append("<ul>");

        for (Source source : battery.getSources()) {
            out.append("<li>")
                    .append("<a href=\"")
                    .append(source.getUrl())
                    .append("\" target=\"_blank\"> ")
                    .append(source.name())
                    .append(" </a>")
                    .append("</li>");
        }
        out.append("</ul>");
        return out.toString();
    }

    private String format1(Double value) {
        return String.format("%.1f", value);
    }

    private String format2(Double value) {
        return format1(value) + "0";
    }

    private Set<String> filterSet(Set<String> set) {
        List<String> out = new ArrayList<>();
        for (String s : set) {
            if (s.isBlank()) {
                continue;
            }
            boolean isShort = true;
            for (String s2 : set) {
                if (s2.isBlank()) {
                    continue;
                }
                if (s.contains(s2) && !s.equals(s2)) {
                    isShort = false;
                    break;
                }
            }
            if (isShort) {
                out.add(s.toUpperCase(Locale.ENGLISH));
            }
        }
        out.sort(Comparator.naturalOrder());
        return new LinkedHashSet<>(out);
    }
}
