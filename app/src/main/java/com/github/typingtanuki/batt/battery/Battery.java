package com.github.typingtanuki.batt.battery;

import com.github.typingtanuki.batt.validator.*;

import java.util.*;

public class Battery {
    private static final Condition[] CONDITIONS = new Condition[]{
            new Condition("pin10",
                    new VoltageValidator(7.4, 8.2),
                    new AmperageValidator(1_000, 1_000_000),
                    new ConnectorValidator(true, BatteryConnector.PIN_10),
                    new TypeValidator(true, BatteryType.LI_POLYMER),
                    new FormValidator(true, BatteryForm.SQUARE, BatteryForm.RECTANGLE, BatteryForm.FAT)),
            new Condition("pin4",
                    new VoltageValidator(11, 15),
                    new AmperageValidator(1_000, 1_000_000),
                    new ConnectorValidator(true, BatteryConnector.PIN_4),
                    new TypeValidator(true, BatteryType.LI_POLYMER),
                    new FormValidator(true, BatteryForm.SQUARE, BatteryForm.RECTANGLE, BatteryForm.FAT))
    };

    private final Set<String> matchedConditions = new HashSet<>();
    private final String currentUrl;
    private final Set<Source> sources = new HashSet<>();
    private final Set<String> brands = new HashSet<>();
    private Double volt;
    private Integer amp;
    private Double watt;
    private int cells;
    private Set<String> partNo;
    private Set<String> models;
    private BatteryType type;
    private BatteryForm form = BatteryForm.UNKNOWN;
    private BatteryConnector connector = BatteryConnector.UNKNOWN;

    public Battery(Source source) {
        super();

        sources.add(source.compact());
        currentUrl = source.getUrl();
    }

    public static String tableHeader() {
        return "| Brand | Power | Cell | Connector | Form factor | Part No. | Models | URL |\r\n" +
                "| ----- | ----- | ---- | --------- | ----------- | -------- | ------ | --- |";
    }

    public Double getVolt() {
        return volt;
    }

    public void setVolt(double volt) {
        this.volt = volt;
    }

    public Integer getAmp() {
        return amp;
    }

    public void setAmp(int amp) {
        this.amp = amp;
    }

    public Double getWatt() {
        return watt;
    }

    public void setWatt(double watt) {
        this.watt = watt;
    }

    public int getCells() {
        return cells;
    }

    public void setCells(int cells) {
        this.cells = cells;
    }

    public String asTable() {
        consolidate();
        return "| " + makeList(brands) +
                " | " + makeList(Arrays.asList(format1(volt) + "V", amp + "mAh", format2(watt) + "W")) +
                " | " + cells +
                " | " + formatEnum(connector) +
                " | " + formatEnum(form) +
                " | " + makeList(partNo) +
                " | " + makeList(models) +
                " | " + formatUrls() +
                " |";
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

    private String formatUrls() {
        StringBuilder out = new StringBuilder();
        out.append("<ul>");

        for (Source source : sources) {
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

    public void setBrand(String brand) {
        for (String b : brand.split(" ")) {
            this.brands.add(b.strip());
        }
    }

    private Set<String> filterSet(Set<String> set) {
        Set<String> out = new LinkedHashSet<>();
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
        return out;
    }

    public boolean isValid() {
        consolidate();
        matchedConditions.clear();

        for (Condition condition : CONDITIONS) {
            if (condition.isValid(this)) {
                matchedConditions.add(condition.getName());
            }
        }

        return !matchedConditions.isEmpty();
    }

    private void consolidate() {
        if (watt == null && amp != null) {
            double comp = amp * volt;
            setWatt(comp / 1000);
        }
        if (amp == null && watt != null) {
            double comp = watt * 100;
            comp = comp / volt;
            setAmp(((int) comp) * 10);
        }
    }

    public BatteryType getType() {
        return type;
    }

    public void setType(BatteryType type) {
        this.type = type;
    }

    public BatteryForm getForm() {
        return form;
    }

    public void setForm(BatteryForm form) {
        this.form = form;
    }

    public BatteryConnector getConnector() {
        return connector;
    }

    public void setConnector(BatteryConnector connector) {
        this.connector = connector;
    }

    public Set<String> getMatchedConditions() {
        return matchedConditions;
    }

    public void mergeWith(Battery battery) {
        partNo.addAll(battery.partNo);
        models.addAll(battery.models);
        sources.addAll(battery.sources);
    }

    public String getCurrentUrl() {
        return currentUrl;
    }

    public String getModel() {
        if (models.isEmpty()) {
            return extractModel(partNo);
        }
        return extractModel(models);
    }

    private String extractModel(Set<String> entries) {
        if (entries.isEmpty()) {
            throw new IllegalStateException("No model");
        }
        List<String> mods = new ArrayList<>(entries);
        mods.sort(Comparator.naturalOrder());
        return mods.get(0)
                .replaceAll("/", "_")
                .replaceAll("\\|", "_");
    }

    public Set<String> getModels() {
        return models;
    }

    public void setModels(Set<String> models) {
        this.models = filterSet(models);
    }

    public Set<String> getPartNo() {
        return partNo;
    }

    public void setPartNo(Set<String> partNo) {
        this.partNo = filterSet(partNo);
    }
}
